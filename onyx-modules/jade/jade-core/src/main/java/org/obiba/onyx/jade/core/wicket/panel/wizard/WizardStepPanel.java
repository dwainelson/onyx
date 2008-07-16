package org.obiba.onyx.jade.core.wicket.panel.wizard;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.panel.Panel;

public abstract class WizardStepPanel extends Panel {

  private static final long serialVersionUID = -6326593868471433867L;

  private WizardForm form;

  WizardStepPanel previous;

  WizardStepPanel next;

  public WizardStepPanel(String id, WizardForm form) {
    super(id);
    this.form = form;
  }

  public void setNextStep(WizardStepPanel next) {
    this.next = next;
  }

  public WizardStepPanel getNextStep() {
    return next;
  }

  public void setPreviousStep(WizardStepPanel previous) {
    this.previous = previous;
  }

  public WizardStepPanel getPreviousStep() {
    return previous;
  }

  public WizardForm getForm() {
    return form;
  }

  public void onStep(WizardForm form, AjaxRequestTarget target) {

  }

  abstract void handleWizardState(WizardForm form, AjaxRequestTarget target);

}
