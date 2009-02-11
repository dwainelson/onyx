/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.jade.core.domain.instrument;

import java.util.List;

import org.obiba.onyx.jade.core.service.InputSourceVisitor;

public class MultipleOutputParameterSource extends InputSource {

  private static final long serialVersionUID = 1L;

  private List<OutputParameterSource> outputParameterSourceList;

  public MultipleOutputParameterSource() {
  }

  @Override
  public void accept(InputSourceVisitor visitor) {
    visitor.visit(this);
  }

  @Override
  public boolean isReadOnly() {
    return true;
  }

  public List<OutputParameterSource> getOutputParameterSourceList() {
    return outputParameterSourceList;
  }

  public void setOutputParameterSourceList(List<OutputParameterSource> outputParameterSourceList) {
    this.outputParameterSourceList = outputParameterSourceList;
  }

}
