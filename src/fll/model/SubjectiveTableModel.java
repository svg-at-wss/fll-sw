/*
 * Copyright (c) 2000-2002 INSciTE.  All rights reserved
 * INSciTE is on the web at: http://www.hightechkids.org
 * This code is released under GPL; see LICENSE.txt for details.
 */
package fll.model;

import java.text.NumberFormat;
import java.text.ParseException;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import javax.swing.table.AbstractTableModel;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import net.mtu.eggplant.util.gui.SortableTableModel;

/**
 * TableModel for entering subjective scores.
 *
 * @version $Revision$
 */
final public class SubjectiveTableModel extends AbstractTableModel implements SortableTableModel {

  /**
   * @param scoreDocument XML document that represents the teams that are
   * being scored alnog with the judges and the current set of scores
   * @param subjectiveElement initial subjective category
   */
  public SubjectiveTableModel(final Document scoreDocument,
                              final Element subjectiveElement) {
    _scoreDocument = scoreDocument;
    setSubjectiveElement(subjectiveElement);
    //by default sort by team number
    _sortedColumn = 0;
    _ascending = true;
    Arrays.sort(_scoreElements, _comparator);
  }

  public String getColumnName(final int column) {
    if(0 == column) {
      return "TeamNumber";
    } else if(1 == column) {
      return "TeamName";
    } else if(2 == column) {
      return "Division";
    } else if(3 == column) {
      return "Judge";
    } else {
      return getGoalDescription(column - 4).getAttribute("title");
    }
  }

  public Class getColumnClass(final int column) {
    return String.class;
  }

  public int getRowCount() {
    return _scoreElements.length;
  }

  public int getColumnCount() {
    return 4 + _subjectiveElement.getElementsByTagName("goal").getLength();
  }

  public Object getValueAt(final int row,
                           final int column) {
    final Element element = getScoreElement(row);
    switch(column) {
    case 0:
      if(element.hasAttribute("teamNumber")) {
        return element.getAttribute("teamNumber");
      } else {
        return "";
      }
    case 1:
      if(element.hasAttribute("teamName")) {
        return element.getAttribute("teamName");
      } else {
        return "";
      }
    case 2:
      return element.getAttribute("division");
    case 3:
      return element.getAttribute("judge");
    default:
      final String goalName = getGoalDescription(column - 4).getAttribute("name");
      if(element.hasAttribute(goalName)) {
        return element.getAttribute(goalName);
      } else {
        return "";
      }
    }
  }
  
  public boolean isCellEditable(final int row,
                                final int column) {
    switch(column) {
    case 0:
    case 1:
    case 2:
    case 3:
      return false;
    default:
      return true;
    }
  }

  public void setValueAt(final Object value,
                         final int row,
                         final int column) {
    setValueAt(value, row, column, true);
  }

  /**
   * Set the value of a cell and only set it's modified flag if setModified is
   * true.  This allows us to use setValueAt to reset incorrect values.
   */
  private void setValueAt(final Object value,
                          final int row,
                          final int column,
                          final boolean setModified) {

    boolean error = false;
    final Element element = getScoreElement(row);
    final Element goalDescription = getGoalDescription(column - 4);

    //support deleting a value
    if(null == value || "".equals(value)) {
      //remove value
      element.setAttribute(goalDescription.getAttribute("name"), "");
      if(setModified) {
        element.setAttribute("modified", Boolean.TRUE.toString());
      }
      return;
    }
    
    if(goalDescription.hasChildNodes()) {
      //enumerated
      boolean found = false;
      Node posValue = goalDescription.getFirstChild();
      while(null != posValue && !found) {
        if("value".equals(posValue.getNodeName())) {
          if(posValue.getFirstChild().getNodeValue().equals(value)) {
            //found it
            element.setAttribute(goalDescription.getAttribute("name"), value.toString());
            if(setModified) {
              element.setAttribute("modified", Boolean.TRUE.toString());
            }
            found = true;
          }
        }
        if(!found) {
          posValue = posValue.getNextSibling();
        }
      }
      if(!found) {
        error = true;
      }
    } else {
      //numeric
        
      int min = 0;
      int max = 1;
      try {
        min = NumberFormat.getInstance().parse(goalDescription.getAttribute("min")).intValue();
        max = NumberFormat.getInstance().parse(goalDescription.getAttribute("max")).intValue();
          
      } catch(final ParseException pe) {
        throw new RuntimeException("Error in challenge.xml!!! min or max unparseable for goal: " + goalDescription.getAttribute("name"));
      }
        
      try {
        final int intValue = NumberFormat.getInstance().parse(value.toString()).intValue();
        if(intValue > max || intValue < min) {
          error = true;
        } else {
          element.setAttribute(goalDescription.getAttribute("name"), String.valueOf(intValue));
          if(setModified) {
            element.setAttribute("modified", Boolean.TRUE.toString());
          }
        }
      } catch(final ParseException pe) {
        error = true;
      }
    }
    
    if(error) {
      //reset
      setValueAt(getValueAt(row, column), row, column, false);
    } else {
      fireTableCellUpdated(row, column);
    }
  }
  

  private Element[] _scoreElements;
  /**
   * Get the score element at index
   */
  private Element getScoreElement(final int index) {
    return _scoreElements[index];
  }

  /**
   * Get the description element for goal at index
   */
  private Element getGoalDescription(final int index) {
    return (Element)_subjectiveElement.getElementsByTagName("goal").item(index);
  }
  
  private Element _subjectiveElement;
  /**
   * Set the subjective element that represents the subjective category to
   * edit.  If this element is already being displayed, this method is a nop.
   *
   * @param subjectiveElement describes the subjective category to edit
   */
  public void setSubjectiveElement(final Element subjectiveElement) {
    if(!subjectiveElement.equals(_subjectiveElement)) {
      _subjectiveElement = subjectiveElement;
      final Element categoryScoreElement = (Element)((Element)_scoreDocument.getDocumentElement()).getElementsByTagName(subjectiveElement.getAttribute("name")).item(0);
      final NodeList scoreElements = categoryScoreElement.getElementsByTagName("score");
      _scoreElements = new Element[scoreElements.getLength()];
      for(int i=0; i<scoreElements.getLength(); i++) {
        _scoreElements[i] = (Element)scoreElements.item(i);
      }
      fireTableStructureChanged();
    }
  }

  private int _sortedColumn = 0;
  public int getSortedColumn() {
    return _sortedColumn;
  }

  private boolean _ascending = true;
  public boolean isAscending() { return _ascending; }
  
  public void sort(final int column) {
    //only sort first 4 columns
    if(column < 4) {
      if(column == _sortedColumn) {
        _ascending = !_ascending;
      } else {
        _sortedColumn = column;
      }
      
      if(isAscending()) {
        Arrays.sort(_scoreElements, _comparator);
      } else {
        Arrays.sort(_scoreElements, _inverseComparator);
      }
      fireTableDataChanged();
    }
  }
  
  /**
   * Get the current subjective element.
   */
  public Element getSubjectiveElement() { return _subjectiveElement; }
  
  /**
   * The backing for the model
   */
  private final Document _scoreDocument;

  private final Comparator _comparator = new Comparator() {
    public int compare(final Object o1, final Object o2) {
      try {
        final Element e1 = (Element)o1;
        final Element e2 = (Element)o2;
        switch(getSortedColumn()) {
        case 0:
          final int team1 = NumberFormat.getInstance().parse(e1.getAttribute("teamNumber")).intValue();
          final int team2 = NumberFormat.getInstance().parse(e2.getAttribute("teamNumber")).intValue();
          if(team1 == team2) {
            return 0;
          } else if(team1 < team2) {
            return -1;
          } else {
            return 1;
          }
        case 1:
          final String name1 = e1.getAttribute("teamName");
          final String name2 = e2.getAttribute("teamName");
          return name1.compareTo(name2);
        case 2:
          final String division1 = e1.getAttribute("division");
          final String division2 = e2.getAttribute("division");
          return division1.compareTo(division2);
        case 3:
          final String judge1 = e1.getAttribute("judge");
          final String judge2 = e2.getAttribute("judge");
          return judge1.compareTo(judge2);
        default:
          //don't sort other columns
          return 0;
        }
      } catch(final ParseException pe) {
        throw new RuntimeException(pe);
      }
    }
  };

  private final Comparator _inverseComparator = new Comparator() {
    public int compare(final Object o1, final Object o2) {
      return -1 * _comparator.compare(o1, o2);
    }
  };
}
