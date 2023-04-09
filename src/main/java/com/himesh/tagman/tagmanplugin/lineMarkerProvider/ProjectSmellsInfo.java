package com.himesh.tagman.tagmanplugin.lineMarkerProvider;

import com.himesh.tagman.tagmanplugin.filesManager.SettingsFolder;
import com.himesh.tagman.tagmanplugin.logger.DesigniteLogger;
import com.himesh.tagman.tagmanplugin.models.DesignSmell;
import com.himesh.tagman.tagmanplugin.models.ImplementationSmell;
import com.himesh.tagman.tagmanplugin.models.TypeMetrics;
import com.intellij.openapi.project.Project;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;


public class ProjectSmellsInfo {
    private final String projectName;
    private final List<ImplementationSmell> implementationSmellsList = new ArrayList<>();
    private final List<DesignSmell> designSmellsList = new ArrayList<>();
    private final List<TypeMetrics> typeMetricsList = new ArrayList<>();
    private final Project project;

    public ProjectSmellsInfo(Project project) {
        this.project = project;
        if (project != null)
            projectName = project.getName();
        else
            projectName = "";
        if (project != null && project.isInitialized()) {
            readImplSmells();
            readDesignSmells();
            readTypeMetrics();
        }
    }

    public Project getProject(){
        return project;
    }
    public boolean isProjectAnalyzed() {
        File designiteOutputFolder = new File(
                String.valueOf(Paths.get(SettingsFolder.getDesigniteOuputDirectoryPath(), projectName)));
        return designiteOutputFolder.exists();
    }

    private void readImplSmells() {
        File designiteOutputFolder = new File(
                String.valueOf(Paths.get(SettingsFolder.getDesigniteOuputDirectoryPath(), projectName)));

        if (designiteOutputFolder.exists()) {
            String implCsvFile = Paths.get(SettingsFolder.getDesigniteOuputDirectoryPath(),
                    projectName,
                    "ImplementationSmells.csv").toString();
            BufferedReader reader;
            try {
                reader = new BufferedReader(new FileReader(implCsvFile));
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] column = line.split(",");
                    if (column.length > 5) {
                        ImplementationSmell smell = new ImplementationSmell();
                        smell.setName(column[4]);
                        smell.setProject(column[0]);
                        smell.setPkg(column[1]);
                        smell.setClassName(column[2]);
                        smell.setMethodName(column[3]);
                        smell.setDescription(column[5]);
                        implementationSmellsList.add(smell);
                    }
                }
            } catch (IOException e) {
                DesigniteLogger.getLogger().log(Level.WARNING, e.getMessage());
                e.printStackTrace();
            }
        }
    }

    public List<ImplementationSmell> getImplementationSmellsList() {
        return implementationSmellsList;
    }

    public List<DesignSmell> getDesignSmellList() {
        return designSmellsList;
    }

    private void readTypeMetrics() {
        File designiteOutputFolder = new File(
                String.valueOf(Paths.get(SettingsFolder.getDesigniteOuputDirectoryPath(), projectName)));
        if (designiteOutputFolder.exists()) {
            String typeMetricsCsv = Paths.get(SettingsFolder.getDesigniteOuputDirectoryPath(),
                    projectName,
                    "TypeMetrics.csv").toString();
            BufferedReader reader = null;
            try {
                reader = new BufferedReader(new FileReader(typeMetricsCsv));
                String line;
                boolean header = true;
                while ((line = reader.readLine()) != null) {
                    if (header) {
                        header = false;
                        continue;
                    }
                    String[] column = line.split(",");
                    if (column.length < 15)
                        continue;
                    TypeMetrics typeMetrics = new TypeMetrics();
                    typeMetrics.ProjectName = column[0];
                    typeMetrics.PackageName = column[1];
                    typeMetrics.ClassName = column[2];
                    typeMetrics.NOF = column[3];
                    typeMetrics.NOPF = column[4];
                    typeMetrics.NOM = column[5];
                    typeMetrics.NOPM = column[6];
                    typeMetrics.LOC = column[7];
                    typeMetrics.WMC = column[8];
                    typeMetrics.NC = column[9];
                    typeMetrics.DIT = column[10];
                    if (column[11].length() > 4)
                        typeMetrics.LCOM = column[11].substring(0, 4);
                    else
                        typeMetrics.LCOM = column[11];
                    typeMetrics.FANIN = column[12];
                    typeMetrics.FANOUT = column[13];
                    typeMetrics.FilePath = column[14];
                    typeMetricsList.add(typeMetrics);
                }
            } catch (IOException e) {
                DesigniteLogger.getLogger().log(Level.WARNING, e.getMessage());
                e.printStackTrace();
            }
        }
    }

    public String getDesigniteOutputFolder() {
        File designiteOutputFolder = new File(
                String.valueOf(Paths.get(SettingsFolder.getDesigniteOuputDirectoryPath(), projectName)));
        if (!designiteOutputFolder.exists()) {
            SettingsFolder.makeProjectOutputPath(
                    Paths.get(SettingsFolder.getDesigniteOuputDirectoryPath(), projectName).toString());
        }
        return Paths.get(SettingsFolder.getDesigniteOuputDirectoryPath(), projectName).toString();
    }

    private void readDesignSmells() {
        File designiteOutputFolder = new File(
                String.valueOf(Paths.get(SettingsFolder.getDesigniteOuputDirectoryPath(), projectName)));

        if (designiteOutputFolder.exists()) {
            String designCsvFile = Paths.get(SettingsFolder.getDesigniteOuputDirectoryPath(),
                    projectName,
                    "DesignSmells.csv").toString();
            BufferedReader reader;
            try {
                reader = new BufferedReader(new FileReader(designCsvFile));
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] column = line.split(",");
                    if (column.length > 4) {
                        DesignSmell smell = new DesignSmell();
                        smell.setName(column[3]);
                        smell.setProject(column[0]);
                        smell.setPkg(column[1]);
                        smell.setClassName(column[2]);
                        smell.setDescription(column[4]);
                        designSmellsList.add(smell);
                    }
                }
            } catch (IOException e) {
                DesigniteLogger.getLogger().log(Level.WARNING, e.getMessage());
                e.printStackTrace();
            }
        }
    }

    public List<TypeMetrics> getTypeMetrics() {
        return typeMetricsList;
    }

}
