/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.quartz.core.engine.questionnaire.util.builder;

import org.apache.wicket.validation.IValidator;
import org.obiba.onyx.quartz.core.engine.questionnaire.answer.AnswerSource;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Category;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.DataValidator;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.OpenAnswerDefinition;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Questionnaire;
import org.obiba.onyx.quartz.core.engine.questionnaire.util.QuestionnaireFinder;
import org.obiba.onyx.util.data.Data;
import org.obiba.onyx.util.data.DataType;

/**
 * {@link OpenAnswerDefinition} builder, given a {@link Questionnaire} and a current {@link Category}.
 * @author Yannick Marcon
 * 
 */
public class OpenAnswerDefinitionBuilder extends AbstractQuestionnaireElementBuilder<OpenAnswerDefinition> {

  /**
   * Constructor using {@link CategoryBuilder} to get the {@link Category} it is applied to.
   * @param parent
   * @param name
   * @param dataType
   */
  private OpenAnswerDefinitionBuilder(CategoryBuilder parent, String name, DataType dataType) {
    super(parent.getQuestionnaire());
    if(!checkUniqueOpenAnswerDefinitionName(name)) {
      throw invalidNameUnicityException(OpenAnswerDefinition.class, name);
    }
    element = new OpenAnswerDefinition(name, dataType);
    parent.getElement().setOpenAnswerDefinition(element);
  }

  /**
   * Set the {@link OpenAnswerDefinition} to the current category.
   * @param name
   * @param dataType
   * @return
   * @throws IllegalArgumentException if name does not respect questionnaire element naming pattern.
   */
  public static OpenAnswerDefinitionBuilder createOpenAnswerDefinition(CategoryBuilder parent, String name, DataType dataType) {
    if(!checkNamePattern(name)) {
      throw invalidNamePatternException(name);
    }
    return new OpenAnswerDefinitionBuilder(parent, name, dataType);
  }

  /**
   * Add a {@link IValidator} to the current {@link OpenAnswerDefinition}.
   * @param validator
   * @return
   */
  public OpenAnswerDefinitionBuilder addOpenAnswerDefintionValidator(DataValidator validator) {
    element.addValidator(validator);
    return this;
  }

  /**
   * Set the unit to the current {@link OpenAnswerDefinition}.
   * @param unit
   * @return
   */
  public OpenAnswerDefinitionBuilder setOpenAnswerDefinitionUnit(String unit) {
    element.setUnit(unit);

    return this;
  }

  /**
   * Set the default data to the current {@link OpenAnswerDefinition}.
   * @param data
   * @return
   */
  public OpenAnswerDefinitionBuilder setOpenAnswerDefinitionDefaultData(Data... defaultValues) {
    for(Data value : defaultValues) {
      element.addDefaultValue(value);
    }

    return this;
  }

  /**
   * Set the default data to the current {@link OpenAnswerDefinition}.
   * @param data
   * @return
   */
  public OpenAnswerDefinitionBuilder setOpenAnswerDefinitionDefaultData(String... defaultValues) {
    for(String value : defaultValues) {
      element.addDefaultValue(value);
    }

    return this;
  }

  /**
   * Set the {@link AnswerSource} associated with the current {@link OpenAnswerDefinition}.
   * @param answerSource
   * @return
   */
  public OpenAnswerDefinitionBuilder setOpenAnswerDefinitionAnswerSource(AnswerSource answerSource) {
    element.setAnswerSource(answerSource);
    return this;
  }

  /**
   * Check open answer definition name unicity.
   * @param name
   * @return
   */
  private boolean checkUniqueOpenAnswerDefinitionName(String name) {
    return (QuestionnaireFinder.getInstance(questionnaire).findOpenAnswerDefinition(name) == null);
  }

}
