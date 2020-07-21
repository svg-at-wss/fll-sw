/*
 * Copyright (c) 2017 High Tech Kids.  All rights reserved
 * HighTechKids is on the web at: http://www.hightechkids.org
 * This code is released under GPL; see LICENSE.txt for details.
 */

package fll.web.schedule;

import java.io.File;
import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import javax.annotation.Nonnull;

import fll.db.CategoryColumnMapping;
import fll.scheduler.ConstraintViolation;
import fll.scheduler.SchedParams;
import fll.scheduler.SubjectiveStation;
import fll.scheduler.TeamScheduleInfo;
import fll.scheduler.TournamentSchedule;

/**
 * Data used in the workflow of uploading a schedule.
 */
public class UploadScheduleData implements Serializable {

  /**
   * Name that instances are referenced as in the session.
   */
  public static final String KEY = "uploadScheduleData";

  private File scheduleFile = null;

  /**
   * @return the file that was uploaded, will be null until set
   */
  public File getScheduleFile() {
    return scheduleFile;
  }

  /**
   * @param v see {@link #getScheduleFile()}
   */
  public void setScheduleFile(@Nonnull final File v) {
    scheduleFile = v;
  }

  /**
   * Used for {@link #setSelectedSheet(String)} when working with a CSV/TSV file.
   */
  public static final String CSV_SHEET_NAME = "csv";

  private String selectedSheet = null;

  /**
   * If {@link #getScheduleFile()} is a spreadsheet, then the selected sheet name.
   *
   * @return null until set
   */
  public String getSelectedSheet() {
    return selectedSheet;
  }

  /**
   * @param v see {@link #getSelectedSheet()}
   */
  public void setSelectedSheet(@Nonnull final String v) {
    selectedSheet = v;
  }

  private final LinkedList<ConstraintViolation> violations = new LinkedList<>();

  /**
   * @return the violations found in the uploaded schedule, initially empty,
   *         unmodifiable collection
   */
  @Nonnull
  public Collection<ConstraintViolation> getViolations() {
    return Collections.unmodifiableCollection(violations);
  }

  /**
   * @param v see {@link #getViolations()}
   */
  public void setViolations(@Nonnull final Collection<ConstraintViolation> v) {
    violations.clear();
    violations.addAll(v);
  }

  private TournamentSchedule schedule = null;

  /**
   * @return the schedule being uploaded, is null until set with
   *         {@link #setSchedule(TournamentSchedule)}
   */
  public TournamentSchedule getSchedule() {
    return schedule;
  }

  /**
   * @param v see {@link #getSchedule()}
   */
  public void setSchedule(@Nonnull final TournamentSchedule v) {
    schedule = v;
  }

  private final LinkedList<CategoryColumnMapping> categoryColumnMappings = new LinkedList<>();

  /**
   * @return the mappings of categories to schedule columns, initially empty,
   *         unmodifiable collection
   */
  @Nonnull
  public Collection<CategoryColumnMapping> getCategoryColumnMappings() {
    return Collections.unmodifiableCollection(categoryColumnMappings);
  }

  /**
   * @param v see {@link #getCategoryColumnMappings()}
   */
  public void setCategoryColumnMappings(@Nonnull final Collection<CategoryColumnMapping> v) {
    categoryColumnMappings.clear();
    categoryColumnMappings.addAll(v);
  }

  private LinkedList<SubjectiveStation> subjectiveStations = null;

  /**
   * @return the subjective stations for the schedule.
   * @see SchedParams#getSubjectiveStations()
   */
  public List<SubjectiveStation> getSubjectiveStations() {
    return schedParams.getSubjectiveStations();
  }

  /**
   * @param v see {@link #getSubjectiveStations()}
   */
  public void setSubjectiveStations(final List<SubjectiveStation> v) {
    schedParams.setSubjectiveStations(v);
  }

  private final LinkedList<String> unusedHeaders = new LinkedList<>();

  /**
   * @return the unused headers from the schedule, initially empty, unmodifiable
   *         list
   */
  @Nonnull
  public List<String> getUnusedHeaders() {
    return Collections.unmodifiableList(unusedHeaders);
  }

  /**
   * @param v see {@link #getUnusedHeaders()}
   */
  @Nonnull
  public void setUnusedHeaders(final List<String> v) {
    unusedHeaders.clear();
    unusedHeaders.addAll(v);
  }

  private SchedParams schedParams = new SchedParams();

  /**
   * This object is used when checking the uploaded schedule for constraint
   * violations.
   * It defaults to the result of {@link SchedParams#SchedParams()}.
   *
   * @return the sched params, not that it is mutable and NOT a copy of the
   *         internal data
   */
  @Nonnull
  public SchedParams getSchedParams() {
    return schedParams;
  }

  /**
   * @param v the new object to use, see {@link #getSchedParams()}
   */
  public void setSchedParams(@Nonnull final SchedParams v) {
    schedParams = v;
  }

  private final LinkedList<TeamScheduleInfo> missingTeams = new LinkedList<>();

  /**
   * @return teams in the schedule and not in the database
   */
  public Collection<TeamScheduleInfo> getMissingTeams() {
    return missingTeams;
  }

  /**
   * @param v see {@link #getMissingTeams()}
   */
  public void setMissingTeams(final Collection<TeamScheduleInfo> v) {
    missingTeams.clear();
    missingTeams.addAll(v);
  }

  private final LinkedList<GatherTeamInformationChanges.TeamNameDifference> nameDifferences = new LinkedList<>();

  /**
   * @return name differences between the schedule and the database
   */
  public Collection<GatherTeamInformationChanges.TeamNameDifference> getNameDifferences() {
    return nameDifferences;
  }

  /**
   * @param v see {@link #getNameDifferences()}
   */
  public void setNameDifferences(final Collection<GatherTeamInformationChanges.TeamNameDifference> v) {
    nameDifferences.clear();
    nameDifferences.addAll(v);
  }

  private final LinkedList<GatherTeamInformationChanges.TeamOrganizationDifference> organizationDifferences = new LinkedList<>();

  /**
   * @return organization differences between the schedule and the database
   */
  public Collection<GatherTeamInformationChanges.TeamOrganizationDifference> getOrganizationDifferences() {
    return organizationDifferences;
  }

  /**
   * @param v see {@link #getOrganizationDifferences()}
   */
  public void setOrganizationDifferences(final Collection<GatherTeamInformationChanges.TeamOrganizationDifference> v) {
    organizationDifferences.clear();
    organizationDifferences.addAll(v);
  }
}
