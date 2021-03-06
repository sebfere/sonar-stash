package org.sonar.plugins.stash.coverage;

import org.sonar.api.BatchComponent;
import org.sonar.api.batch.InstantiationStrategy;
import org.sonar.api.batch.Sensor;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.batch.rule.ActiveRules;
import org.sonar.api.resources.Project;
import org.sonar.plugins.stash.StashPluginConfiguration;
import org.sonar.wsclient.Sonar;

import static org.sonar.plugins.stash.coverage.CoverageUtils.calculateCoverage;
import static org.sonar.plugins.stash.coverage.CoverageUtils.createSonarClient;
import static org.sonar.plugins.stash.coverage.CoverageUtils.getLineCoverage;

@InstantiationStrategy(InstantiationStrategy.PER_BATCH)
public class CoverageProjectStore implements BatchComponent, Sensor {

  private Double previousProjectCoverage = null;
  private int linesToCover = 0;
  private int uncoveredLines = 0;

  private final StashPluginConfiguration config;
  private ActiveRules activeRules;

  public CoverageProjectStore(StashPluginConfiguration config, ActiveRules activeRules) {
    this.config = config;
    this.activeRules = activeRules;
  }

  public Double getProjectCoverage() {
    return calculateCoverage(linesToCover, uncoveredLines);
  }

  public Double getPreviousProjectCoverage() {
    return this.previousProjectCoverage;
  }

  @Override
  public void analyse(Project module, SensorContext context) {
    Sonar sonar = createSonarClient(config);
    this.previousProjectCoverage = getLineCoverage(sonar, module.getEffectiveKey());
  }

  @Override
  public boolean shouldExecuteOnProject(Project project) {
    return CoverageUtils.shouldExecuteCoverage(config, activeRules);
  }

  public void updateMeasurements(int linesToCover, int uncoveredLines) {
    this.linesToCover += linesToCover;
    this.uncoveredLines += uncoveredLines;
  }
}
