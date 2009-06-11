/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.jade.engine.variable.impl;

import java.util.List;

import org.obiba.onyx.core.domain.contraindication.Contraindication;
import org.obiba.onyx.core.domain.participant.Participant;
import org.obiba.onyx.engine.variable.Attribute;
import org.obiba.onyx.engine.variable.Category;
import org.obiba.onyx.engine.variable.IVariablePathNamingStrategy;
import org.obiba.onyx.engine.variable.Variable;
import org.obiba.onyx.engine.variable.VariableData;
import org.obiba.onyx.engine.variable.VariableHelper;
import org.obiba.onyx.jade.core.domain.instrument.Instrument;
import org.obiba.onyx.jade.core.domain.instrument.InstrumentOutputParameter;
import org.obiba.onyx.jade.core.domain.instrument.InstrumentParameter;
import org.obiba.onyx.jade.core.domain.instrument.InstrumentParameterCaptureMethod;
import org.obiba.onyx.jade.core.domain.instrument.InstrumentType;
import org.obiba.onyx.jade.core.domain.instrument.InterpretativeParameter;
import org.obiba.onyx.jade.core.domain.run.InstrumentRun;
import org.obiba.onyx.jade.core.domain.run.InstrumentRunValue;
import org.obiba.onyx.jade.core.domain.run.Measure;
import org.obiba.onyx.jade.core.service.InstrumentRunService;
import org.obiba.onyx.jade.core.service.InstrumentService;
import org.obiba.onyx.jade.engine.variable.IInstrumentTypeToVariableMappingStrategy;
import org.obiba.onyx.util.data.Data;
import org.obiba.onyx.util.data.DataBuilder;
import org.obiba.onyx.util.data.DataType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 */
public class DefaultInstrumentTypeToVariableMappingStrategy implements IInstrumentTypeToVariableMappingStrategy {

  @SuppressWarnings("unused")
  private static final Logger log = LoggerFactory.getLogger(DefaultInstrumentTypeToVariableMappingStrategy.class);

  public static final String INSTRUMENT_RUN = "InstrumentRun";

  public static final String BARCODE = "instrumentBarcode";

  public static final String USER = "user";

  public static final String TIMESTART = "timeStart";

  public static final String TIMEEND = "timeEnd";

  public static final String CONTRAINDICATION = "Contraindication";

  public static final String CONTRAINDICATION_CODE = "code";

  public static final String CONTRAINDICATION_TYPE = "type";

  public static final String OTHER_CONTRAINDICATION = "otherContraindication";

  public static final String MEASURE = "Measure";

  public static final String TIME = "time";

  private InstrumentRunService instrumentRunService;

  private InstrumentService instrumentService;

  private VariableHelper variableHelper;

  public void setInstrumentRunService(InstrumentRunService instrumentRunService) {
    this.instrumentRunService = instrumentRunService;
  }

  public void setInstrumentService(InstrumentService instrumentService) {
    this.instrumentService = instrumentService;
  }

  public Variable getVariable(InstrumentType type) {
    Variable typeVariable = new Variable(type.getName());

    // instrument run
    Variable runVariable = typeVariable.addVariable(new Variable(INSTRUMENT_RUN));

    runVariable.addVariable(new Variable(USER).setDataType(DataType.TEXT));
    runVariable.addVariable(new Variable(TIMESTART).setDataType(DataType.DATE));
    runVariable.addVariable(new Variable(TIMEEND).setDataType(DataType.DATE));
    if(!type.isRepeatable()) {
      runVariable.addVariable(new Variable(BARCODE).setDataType(DataType.TEXT));
    }
    runVariable.addVariable(new Variable(OTHER_CONTRAINDICATION).setDataType(DataType.TEXT));

    Variable ciVariable = runVariable.addVariable(new Variable(CONTRAINDICATION));
    ciVariable.addVariable(new Variable(CONTRAINDICATION_CODE).setDataType(DataType.TEXT));
    ciVariable.addVariable(new Variable(CONTRAINDICATION_TYPE).setDataType(DataType.TEXT));

    // instrument parameters
    List<InstrumentParameter> parameters = type.getInstrumentParameters();
    if(parameters.size() > 0) {
      for(InstrumentParameter parameter : parameters) {

        Variable paramVariable = new Variable(parameter.getCode()).setDataType(parameter.getDataType()).setUnit(parameter.getMeasurementUnit());
        if(type.isRepeatable() && parameter instanceof InstrumentOutputParameter && !parameter.getCaptureMethod().equals(InstrumentParameterCaptureMethod.COMPUTED)) {
          Variable measureVariable = typeVariable.getVariable(MEASURE);
          if(measureVariable == null) {
            measureVariable = typeVariable.addVariable(new Variable(MEASURE).setDataType(DataType.TEXT).setRepeatable(true));
            measureVariable.addVariable(new Variable(USER).setDataType(DataType.TEXT));
            measureVariable.addVariable(new Variable(TIME).setDataType(DataType.DATE));
            measureVariable.addVariable(new Variable(BARCODE).setDataType(DataType.TEXT));
          }
          measureVariable.addVariable(paramVariable);
        } else {
          typeVariable.addVariable(paramVariable);
          if(parameter instanceof InterpretativeParameter) {
            paramVariable.addCategories(new Category(InterpretativeParameter.YES, "1"), new Category(InterpretativeParameter.NO, "0"));
          } else if(parameter.getAllowedValues().size() > 0) {
            int pos = 1;
            for(Data allowedValue : parameter.getAllowedValues()) {
              String code = allowedValue.getValueAsString();
              Category category = new Category(code, Integer.toString(pos++));
              addLocalizedAttributes(category, code);
              paramVariable.addCategory(category);
            }
          }
        }

        addLocalizedAttributes(paramVariable, parameter.getCode());

        if(parameter.getIntegrityChecks().size() > 0) {
          paramVariable.addAttributes(new Attribute("validation", parameter.getIntegrityChecks().toString()));
        }

        if(parameter.getDataSource() != null) {
          paramVariable.addAttributes(new Attribute("source", parameter.getDataSource().toString()));
        }

        if(parameter.getCondition() != null) {
          paramVariable.addAttributes(new Attribute("condition", parameter.getCondition().toString()));
        }
      }
    }
    return typeVariable;
  }

  public VariableData getVariableData(Participant participant, Variable variable, IVariablePathNamingStrategy variablePathNamingStrategy, VariableData varData) {
    // variable is expected to be a terminal one
    if(variable == null || variable.getDataType() == null) {
      return varData;
    }

    if(variable instanceof Category) {
      // parameter name is the parent variable
      String parameterCode = variable.getParent().getName();
      Data data = getInstrumentRunValue(participant, variable, parameterCode);
      if(data != null && variable.getName().equals(data.getValueAsString())) {
        varData.addData(DataBuilder.buildBoolean(true));
      }
    } else if(variable.getParent().getName().equals(MEASURE) || variable.getName().equals(MEASURE)) {
      InstrumentRun run = getInstrumentRun(participant, getInstrumentTypeVariable(variable).getName());
      if(run != null) {
        InstrumentType type = instrumentService.getInstrumentType(getInstrumentTypeVariable(variable).getName());
        List<Measure> measures = run.getMeasures();

        int measurePosition = 1;
        for(Measure measure : measures) {
          Data data = null;

          if(variable.getName().equals(MEASURE)) {
            varData.addData(DataBuilder.buildText(measure.getId().toString()));
          } else if(variable.getName().equals(USER) && measure.getUser() != null) {
            data = DataBuilder.buildText(measure.getUser().getLogin());
          } else if(variable.getName().equals(TIME) && measure.getTime() != null) {
            data = DataBuilder.buildDate(measure.getTime());
          } else if(variable.getName().equals(BARCODE) && measure.getInstrumentBarcode() != null) {
            data = DataBuilder.buildText(measure.getInstrumentBarcode());
          } else {
            // variable name is the parameter name
            String parameterCode = variable.getName();
            InstrumentParameter parameter = type.getInstrumentParameter(parameterCode);
            InstrumentRunValue runValue = instrumentRunService.findInstrumentRunValue(participant, type, parameterCode, measurePosition);
            if(runValue != null) {
              data = runValue.getData(parameter.getDataType());
            }
          }

          if(data != null) {
            VariableData childVarData = new VariableData(variablePathNamingStrategy.getPath(variable, MEASURE, measure.getId().toString()));
            varData.addVariableData(childVarData);
            childVarData.addData(data);
          }

          measurePosition++;
        }
      }
    } else if(variable.getParent().getName().equals(CONTRAINDICATION)) {
      InstrumentRun run = getInstrumentRun(participant, getInstrumentTypeVariable(variable).getName());
      if(run != null) {
        if(variable.getName().equals(CONTRAINDICATION_CODE) && run.getContraindication() != null) {
          varData.addData(DataBuilder.buildText(run.getContraindication()));
        } else if(variable.getName().equals(CONTRAINDICATION_TYPE) && run.getContraindication() != null) {
          InstrumentType instrumentType = instrumentService.getInstrumentType(getInstrumentTypeVariable(variable).getName());
          Contraindication contraindication = instrumentType.getContraindication(run.getContraindication());
          varData.addData(DataBuilder.buildText(contraindication.getType().toString()));
        }
      }
    } else if(variable.getParent().getName().equals(INSTRUMENT_RUN)) {
      InstrumentRun run = getInstrumentRun(participant, getInstrumentTypeVariable(variable).getName());
      if(run != null) {
        if(variable.getName().equals(USER) && run.getUser() != null) {
          varData.addData(DataBuilder.buildText(run.getUser().getLogin()));
        } else if(variable.getName().equals(TIMESTART) && run.getTimeStart() != null) {
          varData.addData(DataBuilder.buildDate(run.getTimeStart()));
        } else if(variable.getName().equals(TIMEEND) && run.getTimeEnd() != null) {
          varData.addData(DataBuilder.buildDate(run.getTimeEnd()));
        } else if(variable.getName().equals(BARCODE) && run.getInstrument() != null) {
          Instrument instrument = run.getInstrument();
          if(instrument != null && instrument.getBarcode() != null) {
            varData.addData(DataBuilder.buildText(instrument.getBarcode()));
          }
        } else if(variable.getName().equals(OTHER_CONTRAINDICATION) && run.getOtherContraindication() != null) {
          varData.addData(DataBuilder.buildText(run.getOtherContraindication()));
        }
      }
    } else {
      Data data = getInstrumentRunValue(participant, variable, variable.getName());
      if(data != null) {
        varData.addData(data);
      }
    }

    return varData;
  }

  private Data getInstrumentRunValue(Participant participant, Variable variable, String parameterCode) {
    String instrumentTypeName = getInstrumentTypeVariable(variable).getName();

    InstrumentType type = instrumentService.getInstrumentType(instrumentTypeName);
    if(type != null) {

      InstrumentParameter parameter = type.getInstrumentParameter(parameterCode);
      InstrumentRunValue runValue = instrumentRunService.findInstrumentRunValue(participant, type, parameterCode, null);

      if(runValue != null) {
        return runValue.getData(parameter.getDataType());
      }
    }

    return null;
  }

  private InstrumentRun getInstrumentRun(Participant participant, String instrumentTypeName) {
    InstrumentType type = instrumentService.getInstrumentType(instrumentTypeName);
    return instrumentRunService.getLastCompletedInstrumentRun(participant, type);
  }

  public Variable getInstrumentTypeVariable(Variable variable) {
    Variable instrumentTypeVariable = variable;

    while(instrumentTypeVariable.getParent() != null && instrumentTypeVariable.getParent().getParent() != null) {
      instrumentTypeVariable = instrumentTypeVariable.getParent();
    }

    return instrumentTypeVariable;
  }

  public void setVariableHelper(VariableHelper variableHelper) {
    this.variableHelper = variableHelper;
  }

  private void addLocalizedAttributes(Variable variable, String property) {
    if(variableHelper != null) {
      variableHelper.addLocalizedAttributes(variable, property);
    }
  }

}
