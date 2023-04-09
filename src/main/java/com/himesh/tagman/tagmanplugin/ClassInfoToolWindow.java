package com.himesh.tagman.tagmanplugin;

import com.himesh.tagman.tagmanplugin.lineMarkerProvider.ProjectSmellsInfo;
import com.himesh.tagman.tagmanplugin.lineMarkerProvider.SmellsInfoProvider;
import com.himesh.tagman.tagmanplugin.models.DesignSmell;
import com.himesh.tagman.tagmanplugin.models.ImplementationSmell;
import com.himesh.tagman.tagmanplugin.models.TypeMetrics;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.treeStructure.Tree;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

import static com.himesh.tagman.tagmanplugin.constants.Constants.TOOL_WINDOW_ANALYSIS_TEXT;
import static com.himesh.tagman.tagmanplugin.constants.Constants.TOOL_WINDOW_TEXT;


public class ClassInfoToolWindow {
    private final ToolWindowFactory toolWindowFactory;
    private String currentFocusedFile;
    private Project project;

    public ClassInfoToolWindow(ToolWindowFactory toolWindowFactory) {
        this.toolWindowFactory = toolWindowFactory;
        currentFocusedFile = "";
    }

    @NotNull
    private synchronized Tree getTree(Project project) {
        ProjectSmellsInfo smellsInfoProvider = SmellsInfoProvider.getInstance(project);
        List<TypeMetrics> typeMetricsList = smellsInfoProvider.getTypeMetrics();
//        JOptionPane.showMessageDialog(null, "total: " + typeMetricsList.size(), "DesigniteJava: Total type metrics",
//                JOptionPane.WARNING_MESSAGE);
        if (currentFocusedFile.isEmpty()) {
            DefaultMutableTreeNode currentClassDoc = new DefaultMutableTreeNode("DesigniteJava code quality window");
            currentClassDoc.add(new DefaultMutableTreeNode(TOOL_WINDOW_TEXT));
            currentClassDoc.add(new DefaultMutableTreeNode(TOOL_WINDOW_ANALYSIS_TEXT));
            return new Tree(currentClassDoc);
        } else {
            List<TypeMetrics> filteredList = typeMetricsList
                    .stream()
                    .filter(metrics -> currentFocusedFile.equals(metrics.FilePath))
                    .collect(Collectors.toList());
//            JOptionPane.showMessageDialog(null, "total: " + filteredList.size() + "\ncurrentFocusedFile: " + currentFocusedFile + "\nfirst FilePath: " + typeMetricsList.get(0).FilePath, "DesigniteJava: Total type metrics",
//                    JOptionPane.WARNING_MESSAGE);
            DefaultMutableTreeNode currentDoc = new DefaultMutableTreeNode("Code Quality information about the selected document");

            DefaultMutableTreeNode firstClass = null;
            for (TypeMetrics typeMetrics : filteredList) {
                DefaultMutableTreeNode currentClass = new DefaultMutableTreeNode("Class Info - " + typeMetrics.ClassName);
                populateDesignSmells(smellsInfoProvider, typeMetrics, currentClass);
                populateImplSmells(smellsInfoProvider, typeMetrics, currentClass);
                populateMetrics(typeMetrics, currentClass);
                currentDoc.add(currentClass);
                if (firstClass == null)
                    firstClass = currentClass;
            }
            Tree tree = new Tree(currentDoc);
            if (firstClass != null)
                tree.expandPath(new TreePath(firstClass.getPath()));
            else {
//                if (!smellsInfoProvider.isProjectAnalyzed()) {
//                    DesigniteMenuAnalyze menuAnalyze = new DesigniteMenuAnalyze();
//                    menuAnalyze.actionPerformed(null);
//                }
                DefaultMutableTreeNode currentClassDoc = new DefaultMutableTreeNode("DesigniteJava code quality window");
                currentClassDoc.add(new DefaultMutableTreeNode(TOOL_WINDOW_TEXT));
                currentClassDoc.add(new DefaultMutableTreeNode(TOOL_WINDOW_ANALYSIS_TEXT));
                tree = new Tree(currentClassDoc);
            }
            return tree;
        }
    }

    private boolean isProjectInitiated() {
        ProjectManager projectManager = ProjectManager.getInstance();
        Project[] projects = projectManager.getOpenProjects();
        Project project = null;
        if (projects.length > 0) {
            project = projects[projects.length - 1];
            return project.isOpen();
        }
        return false;
    }

    private void populateMetrics(TypeMetrics typeMetrics, DefaultMutableTreeNode currentClass) {
        DefaultMutableTreeNode metricsList = new DefaultMutableTreeNode("Metrics");
        DefaultMutableTreeNode[] metrics = {
                new DefaultMutableTreeNode("Lines of code: " + typeMetrics.LOC),
                new DefaultMutableTreeNode("Lack of cohesion of methods: " + typeMetrics.LCOM),
                new DefaultMutableTreeNode("Weighted methods per class: " + typeMetrics.WMC),
                new DefaultMutableTreeNode("Number of fields: " + typeMetrics.NOF),
                new DefaultMutableTreeNode("Number of public fields: " + typeMetrics.NOPF),
                new DefaultMutableTreeNode("Number of methods: " + typeMetrics.NOM),
                new DefaultMutableTreeNode("Number of public methods: " + typeMetrics.NOPM),
                new DefaultMutableTreeNode("Fan-in: " + typeMetrics.FANIN),
                new DefaultMutableTreeNode("Fan-out: " + typeMetrics.FANOUT),
                new DefaultMutableTreeNode("Depth of inheritance tree: " + typeMetrics.DIT),
                new DefaultMutableTreeNode("Number of children (subtypes): " + typeMetrics.NC)
        };
        for (int i = 0; i <= metrics.length - 1; i++) {
            metricsList.add(metrics[i]);
        }
        currentClass.add(metricsList);
    }

    private void populateDesignSmells(ProjectSmellsInfo smellsInfoProvider, TypeMetrics typeMetrics, DefaultMutableTreeNode currentClass) {
        List<DesignSmell> designSmellList = smellsInfoProvider.getDesignSmellList();
        List<DesignSmell> filteredSmellList = designSmellList
                .stream()
                .filter(smell -> smell.getClassName().equals(typeMetrics.ClassName) && smell.getPkg().equals(typeMetrics.PackageName))
                .collect(Collectors.toList());
        if (filteredSmellList.size() > 0) {
            DefaultMutableTreeNode designSmellsListText = new DefaultMutableTreeNode("Design smells");

            for (DesignSmell designSmell : filteredSmellList) {
                DefaultMutableTreeNode aDesignSmell = new DefaultMutableTreeNode(designSmell.getName() + ": " + designSmell.getDescription());
                designSmellsListText.add(aDesignSmell);
            }
            currentClass.add(designSmellsListText);
        }
    }

    private void populateImplSmells(ProjectSmellsInfo smellsInfoProvider, TypeMetrics typeMetrics, DefaultMutableTreeNode currentClass) {
        List<ImplementationSmell> implementationSmellList = smellsInfoProvider.getImplementationSmellsList();
        List<ImplementationSmell> filteredImplSmellList = implementationSmellList
                .stream()
                .filter(smell -> smell.getClassName().equals(typeMetrics.ClassName) && smell.getPkg().equals(typeMetrics.PackageName))
                .collect(Collectors.toList());
        if (filteredImplSmellList.size() > 0) {
            DefaultMutableTreeNode implSmellsListText = new DefaultMutableTreeNode("Implementation smells");

            for (ImplementationSmell implSmell : filteredImplSmellList) {
                DefaultMutableTreeNode aImplSmell = new DefaultMutableTreeNode(implSmell.getName() + " [in method " +
                        implSmell.getMethodName() + "]: " + implSmell.getDescription());
                implSmellsListText.add(aImplSmell);
            }
            currentClass.add(implSmellsListText);
        }
    }

    public JPanel getContent(Project project) {
        Tree designiteTree = getTree(project);
        this.project = project;
        JBScrollPane scrollPane = new JBScrollPane(designiteTree);
        JPanel panel = new JPanel(new GridLayout(0, 1));
        panel.add(scrollPane);
        return panel;
    }

    public void update(String path) {
        if (project != null) {
            currentFocusedFile = path.replace("/", File.separator);
            toolWindowFactory.setContent(getContent(project));
        }
    }
}