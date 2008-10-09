package org.obiba.onyx.quartz.core.engine.questionnaire.question;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Page implements Serializable, ILocalizable {

  private static final long serialVersionUID = -7732601103831162009L;

  private String name;

  private Section section;

  private List<Question> questions;
  
  public Page(String name) {
    this.name = name;
  }

  public Section getSection() {
    return section;
  }

  public void setSection(Section section) {
    this.section = section;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public List<Question> getQuestions() {
    return questions != null ? questions : (questions = new ArrayList<Question>());
  }

  public void addQuestion(Question question) {
    if(question != null) {
      getQuestions().add(question);
      question.setPage(this);
    }
  }

  private static final String[] PROPERTIES = { "label" };

  public String getPropertyKey(String property) {
    for(String key : PROPERTIES) {
      if(key.equals(property)) {
        return getClass().getSimpleName() + "." + getName() + "." + property;
      }
    }
    throw new IllegalArgumentException("Invalid property for class " + getClass().getName() + ": " + property);
  }
  
  public String[] getProperties() {
    return PROPERTIES;
  }

}
