package com.sun.enterprise.build;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.model.*;
import org.apache.maven.project.MavenProject;

import java.io.PrintWriter;
import java.util.List;

/**
 * Interface to create the dashboard, implementation are rendering specifics (html, wiki)
 *
 * Each instance is not thread-safe.
 *
 * @author Jerome Dochez
 */
public interface DistributionVisitor {

    /**
     * Sets the writer associated with his generator
     *
     * @param writer the writer associated with this generator
     * @param distribution the distribution's project
     */
    public void beginDistribution(PrintWriter writer, MavenProject distribution);


    /**
     * Start a new category for the dashboard, usually this is represented
     * by a separate table but the underlying format will decide.
     *
     * @param categoryName name of the new category
     */
    public void beginCategory(String categoryName);

    /**
     * Start a new group of modules or librairies.
     *
     * @param groupName the new group name
     * @param inTable true if the group should be displayed as a column
     * or as a subtitle.
     */
    public void beginGroup(String groupName, boolean inTable);

    /**
     * Start a new module/library artifact.
     *
     * @param artifact artifact for the module
     */
    public void beginArtifact(Artifact artifact);

    /**
     * Adds the module's size
     * @param size module's size
     */
    public void addSize(long size);

    /**
     * Adds the module's repository
     * @param repository module's repository
     */
    public void addRepository(ArtifactRepository repository);

    /**
     * Adds the list of developers of the module
     * @param devs module's developers
     */
    public void addDevelopers(List<Developer> devs);

    /**
     * Adds the list of mailing-lists associated with the module
     *
     * @param lists mailing-lists for the module
     */
    public void addMailingLists(List<MailingList> lists);

    /**
     * Adds the list of imported modules by this module
     * @param dependencies module's dependencies
     */
    public void addImports(List<Dependency> dependencies);

    /**
     * Adds the list of modules importing the current module
     *
     * @param importers module's importers
     */
    public void addImportedBy(List<Artifact> importers);

    /**
     * Adds the source code management information
     *
     * @param scm module's SCM
     */
    public void addSCM(Scm scm);

    /**
     * Adds the licenses governing the module
     * @param licenses module's licenses
     */
    public void addLicenses(List<License> licenses);

    /**
     * End of the current module's model
     */
    public void endArtifact();

    /**
     * End of the current group
     */
    public void endGroup();

    /**
     * End of the current category
     */
    public void endCategory();

    /**
     * End of the distribution
     */
    public void endDistribution();
}
