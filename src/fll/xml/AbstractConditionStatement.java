/*
 * Copyright (c) 2013 INSciTE.  All rights reserved
 * INSciTE is on the web at: http://www.hightechkids.org
 * This code is released under GPL; see LICENSE.txt for details.
 */

package fll.xml;

import java.io.Serializable;

import net.mtu.eggplant.xml.NodelistElementCollectionAdapter;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import fll.util.FLLInternalException;
import fll.web.playoff.TeamScore;

/**
 * Common parent class of {@link ConditionStatement} and
 * {@link EnumConditionStatement}.
 */
public abstract class AbstractConditionStatement implements Serializable {

  public AbstractConditionStatement(final Element ele) {
    if (new NodelistElementCollectionAdapter(ele.getElementsByTagName(InequalityComparison.LESS_THAN_TAG_NAME)).hasNext()) {
      mComparison = InequalityComparison.LESS_THAN;
    } else if (new NodelistElementCollectionAdapter(ele.getElementsByTagName(InequalityComparison.LESS_THAN_OR_EQUAL_TAG_NAME)).hasNext()) {
      mComparison = InequalityComparison.LESS_THAN_OR_EQUAL;
    } else if (new NodelistElementCollectionAdapter(ele.getElementsByTagName(InequalityComparison.GREATER_THAN_TAG_NAME)).hasNext()) {
      mComparison = InequalityComparison.GREATER_THAN;
    } else if (new NodelistElementCollectionAdapter(ele.getElementsByTagName(InequalityComparison.GREATER_THAN_OR_EQUAL_TAG_NAME)).hasNext()) {
      mComparison = InequalityComparison.GREATER_THAN_OR_EQUAL;
    } else if (new NodelistElementCollectionAdapter(ele.getElementsByTagName(InequalityComparison.EQUAL_TO_TAG_NAME)).hasNext()) {
      mComparison = InequalityComparison.EQUAL_TO;
    } else if (new NodelistElementCollectionAdapter(ele.getElementsByTagName(InequalityComparison.NOT_EQUAL_TO_TAG_NAME)).hasNext()) {
      mComparison = InequalityComparison.NOT_EQUAL_TO;
    } else {
      throw new FLLInternalException("Unknown comparison");
    }
  }

  private InequalityComparison mComparison;

  public InequalityComparison getComparison() {
    return mComparison;
  }

  public void setComparison(final InequalityComparison v) {
    mComparison = v;
  }

  /**
   * Does this conditional statement evaluate to true?
   */
  public abstract boolean isTrue(TeamScore teamScore);

  public Element getComparisonElement(final Document doc) {
    switch (mComparison) {
    case LESS_THAN:
      return doc.createElement(InequalityComparison.LESS_THAN_TAG_NAME);
    case LESS_THAN_OR_EQUAL:
      return doc.createElement(InequalityComparison.LESS_THAN_OR_EQUAL_TAG_NAME);
    case GREATER_THAN:
      return doc.createElement(InequalityComparison.GREATER_THAN_TAG_NAME);
    case GREATER_THAN_OR_EQUAL:
      return doc.createElement(InequalityComparison.GREATER_THAN_OR_EQUAL_TAG_NAME);
    case EQUAL_TO:
      return doc.createElement(InequalityComparison.EQUAL_TO_TAG_NAME);
    case NOT_EQUAL_TO:
      return doc.createElement(InequalityComparison.NOT_EQUAL_TO_TAG_NAME);
    default:
      throw new FLLInternalException("Unknown comparison");
    }
  }

  public abstract Element toXml(final Document doc);

}
