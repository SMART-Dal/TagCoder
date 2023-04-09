package com.himesh.tagman.tagmanplugin;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.himesh.tagman.tagmanplugin.assetLoaders.DesigniteAssetLoader;
import com.himesh.tagman.tagmanplugin.lineMarkerProvider.ProjectSmellsInfo;
import com.himesh.tagman.tagmanplugin.lineMarkerProvider.SmellsInfoProvider;
import com.himesh.tagman.tagmanplugin.logger.DesigniteLogger;
import com.himesh.tagman.tagmanplugin.models.CodeSmell;
import com.himesh.tagman.tagmanplugin.models.DesignSmell;
import com.himesh.tagman.tagmanplugin.models.ImplementationSmell;
import com.himesh.tagman.tagmanplugin.utils.IntelliJ_Utils;
import com.intellij.codeHighlighting.Pass;
import com.intellij.codeInsight.daemon.GutterIconNavigationHandler;
import com.intellij.codeInsight.daemon.RelatedItemLineMarkerInfo;
import com.intellij.codeInsight.daemon.RelatedItemLineMarkerProvider;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.editor.markup.GutterIconRenderer;
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.util.NotNullLazyValue;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMethod;
import com.intellij.ui.ColoredListCellRenderer;
import com.intellij.ui.components.JBList;
import com.intellij.util.FunctionUtil;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.*;
import java.util.List;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class TagmanLineMarker extends RelatedItemLineMarkerProvider {
    DesigniteAssetLoader designiteAssets = new DesigniteAssetLoader();
    //ProjectSmellsInfo smellsInfoProvider = null;

    protected void collectNavigationMarkers(@NotNull PsiElement element, @NotNull Collection<? super RelatedItemLineMarkerInfo<?>> result) {
        //DesigniteLogger.getLogger().log(Level.INFO, "Invoked.");
        ProjectSmellsInfo smellsInfoProvider = SmellsInfoProvider.getInstance(element.getProject());
        //if (smellsInfoProvider.getProject() != null)
        //    DesigniteLogger.getLogger().log(Level.INFO, "fetching info for " + smellsInfoProvider.getProject().getName());

        if (element instanceof PsiClass) {
            PsiClass psiClass = (PsiClass) element;
            List<DesignSmell> designSmellList = smellsInfoProvider.getDesignSmellList();
            if (psiClass.getQualifiedName() == null) return;
            String currentClass = psiClass.getQualifiedName();
            List<CodeSmell> filteredList = designSmellList.stream().filter(smell -> psiClass.getQualifiedName().equals(smell.getPkg() + "." + smell.getClassName())).collect(Collectors.toList());
            if (filteredList.size() > 0) {
                //  MyLineMarkerInfo info = new MyLineMarkerInfo(element, designiteAssets, filteredList, "design");
                // result.add(info);
            }
            try {
                String stubsApiBaseUri = "http://localhost:8031/api/apply-ml/";
                HttpClient client = HttpClients.createDefault();
                URIBuilder builder = new URIBuilder(stubsApiBaseUri);
                builder.addParameter("file", psiClass.getContainingFile().getText());
                builder.addParameter("isClass", "True");
                builder.addParameter("qaulifiedName", currentClass);
                builder.addParameter("className", psiClass.getName());
                String listStubsUri = builder.build().toString();
                HttpPost getStubMethod = new HttpPost(listStubsUri);
                HttpResponse getStubResponse = client.execute(getStubMethod);
                int getStubStatusCode = getStubResponse.getStatusLine().getStatusCode();
                if (getStubStatusCode > 200 && getStubStatusCode <= 300) {
                    // Handle non-2xx status code
                    return;
                }
                HttpEntity entity = getStubResponse.getEntity();
                if (entity != null) {
                    String content = EntityUtils.toString(entity);
                    JsonObject jsonObject = new JsonParser().parse(content).getAsJsonObject();
                    String smellStr = jsonObject.get("isSmell").getAsString();
                    List<String> smellsStr = List.of(smellStr.split(";"));
                    List<Boolean> smellsRes = smellsStr.stream().map(smell -> Boolean.parseBoolean(smell)).collect(Collectors.toList());
                    boolean isSmell = Boolean.parseBoolean(jsonObject.get("isSmell").getAsString());
                    String smell = jsonObject.get("smell").getAsString();
                    List<String> smells = List.of(smell.split(";"));
                    System.out.println("isSmell" + isSmell);
                    //System.out.println("psitoken" + psiMethod.getContainingFile().getVirtualFile().getPath());
                    MyLineMarkerInfo info = new MyLineMarkerInfo(element, designiteAssets, smells, smellsRes, result);
                    result.add(info);
                    // do something with the JSON object
                }
                // List<CodeSmell> filteredList = implementationSmellList.stream().filter(smell -> currentClass.equals(smell.getPkg() + "." + smell.getClassName()) && psiMethod.getName().equals(smell.getMethodName())).collect(Collectors.toList());

//                if (filteredList.size() > 0) {
//                    psiMethod.getContainingFile().getVirtualFile().getPath();
//                    System.out.println("psitoken" + psiMethod.getContainingFile().getVirtualFile().getPath());
//                    MyLineMarkerInfo info = new MyLineMarkerInfo(element, designiteAssets, filteredList, "implementation");
//                    result.add(info);
//                }
            } catch (Exception ex) {
                DesigniteLogger.getLogger().log(Level.WARNING, ex.getMessage());
            }
        }

        if (element instanceof PsiMethod) {
            List<ImplementationSmell> implementationSmellList = smellsInfoProvider.getImplementationSmellsList();
            PsiMethod psiMethod = (PsiMethod) element;
            String currentClass = Objects.requireNonNull(psiMethod.getContainingClass()).getQualifiedName();
            if (currentClass == null) return;
            try {
                String stubsApiBaseUri = "http://localhost:8031/api/apply-ml/";
                HttpClient client = HttpClients.createDefault();
                URIBuilder builder = new URIBuilder(stubsApiBaseUri);
                builder.addParameter("file", psiMethod.getContainingFile().getText());
                builder.addParameter("isClass", "False");
                builder.addParameter("qaulifiedName", currentClass);
                builder.addParameter("className", psiMethod.getName());
                String listStubsUri = builder.build().toString();
                HttpPost getStubMethod = new HttpPost(listStubsUri);
                HttpResponse getStubResponse = client.execute(getStubMethod);
                int getStubStatusCode = getStubResponse.getStatusLine().getStatusCode();
                if (getStubStatusCode > 200 && getStubStatusCode <= 300) {
                    // Handle non-2xx status code
                    return;
                }
                HttpEntity entity = getStubResponse.getEntity();
                if (entity != null) {
                    String content = EntityUtils.toString(entity);
                    JsonObject jsonObject = new JsonParser().parse(content).getAsJsonObject();
                    String smellStr = jsonObject.get("isSmell").getAsString();
                    List<String> smellsStr = List.of(smellStr.split(";"));
                    List<Boolean> smellsRes = smellsStr.stream().map(smell -> Boolean.parseBoolean(smell)).collect(Collectors.toList());
                    boolean isSmell = Boolean.parseBoolean(jsonObject.get("isSmell").getAsString());
                    String smell = jsonObject.get("smell").getAsString();
                    List<String> smells = List.of(smell.split(";"));
                    System.out.println("isSmell" + isSmell);
                    //System.out.println("psitoken" + psiMethod.getContainingFile().getVirtualFile().getPath());
                    MyLineMarkerInfo info = new MyLineMarkerInfo(element, designiteAssets, smells, smellsRes, result);
                    result.add(info);
                    // do something with the JSON object
                }
                List<CodeSmell> filteredList = implementationSmellList.stream().filter(smell -> currentClass.equals(smell.getPkg() + "." + smell.getClassName()) && psiMethod.getName().equals(smell.getMethodName())).collect(Collectors.toList());

//                if (filteredList.size() > 0) {
//                    psiMethod.getContainingFile().getVirtualFile().getPath();
//                    System.out.println("psitoken" + psiMethod.getContainingFile().getVirtualFile().getPath());
//                    MyLineMarkerInfo info = new MyLineMarkerInfo(element, designiteAssets, filteredList, "implementation");
//                    result.add(info);
//                }
            } catch (Exception ex) {
                DesigniteLogger.getLogger().log(Level.WARNING, ex.getMessage());
            }
        }
    }

    private static class MyLineMarkerInfo extends RelatedItemLineMarkerInfo<PsiElement> {
        private final List<String> smellList;

        private final List<Boolean> smellBoolean;

        private final Collection<? super RelatedItemLineMarkerInfo<?>> result;
        PsiElement psiElement;



        private MyLineMarkerInfo(@NotNull PsiElement element, DesigniteAssetLoader designiteAssetLoader, List<String> smellList, List<Boolean> smellBoolean, Collection<? super RelatedItemLineMarkerInfo<?>> result) {
            super(element, element.getTextRange(), designiteAssetLoader.getDesigniteIcon(), Pass.LOCAL_INSPECTIONS, FunctionUtil.constant(smellList.size() + " smell(s) detected."),   new GutterIconNavigationHandler<PsiElement>() {
                private final Icon myClickedGutterIcon = designiteAssetLoader.getDesigniteIconGreen();
                @Override
                public void navigate(MouseEvent e, PsiElement elt) {
                    // Code to handle the gutter icon click
                    // For example, you can display a popup dialog box here
                    System.out.println("Gutter icon clicked!");

                    // Change the gutter icon to the clicked icon
                    ((MyGutterIconRenderer) e.getSource()).setIcon(myClickedGutterIcon);
                }
            }, GutterIconRenderer.Alignment.RIGHT, NotNullLazyValue.createConstantValue(Collections.emptyList()));
            this.smellList = smellList;
            this.psiElement = element;
            this.smellBoolean = smellBoolean;
            this.result = result;
        }

        @Override
        public GutterIconRenderer createGutterRenderer() {
            if (myIcon == null) return null;
            return new LineMarkerGutterIconRenderer<PsiElement>(this) {

                @Override
                public AnAction getClickAction() {

                    return new SimplePopDialogAction(smellList, smellBoolean, psiElement, result);
                }
            };
        }
    }

    public static class SimplePopDialogAction extends AnAction {
        private final List<String> smellList;
        private final List<Boolean> smellBol;
        private final PsiElement psiElement;

        Collection<? super RelatedItemLineMarkerInfo<?>> resultPassed;

        public SimplePopDialogAction(List<String> smellList, List<Boolean> smellBol, PsiElement psiElement, Collection<? super RelatedItemLineMarkerInfo<?>> result) {
            this.smellList = smellList;
            this.psiElement = psiElement;
            this.smellBol = smellBol;
            this.resultPassed = result;
        }

        @Override
        public void actionPerformed(@NotNull AnActionEvent e) {
            String infoMessage = "<html><body>";
            String[] options = {"Yes", "No!"};
            for (int i = 0; i < smellList.size(); i++) {
                if (smellList.get(i).equalsIgnoreCase("no smell")) {
                    infoMessage += "<p style='width: 400px;'><b>" + smellList.get(i) + " was identified" + ". </b> " + "</p>";
                    //        infoMessage += "<p style='width: 400px;'><b>If you think a smell exisits, please select from the list below.</b></p><br>";
//                    JPanel pContainer = new JPanel();
//
//
//                    pContainer.setLayout(new BoxLayout(pContainer, BoxLayout.Y_AXIS));
//                    JLabel label = new JLabel(infoMessage);
//                    label.setAlignmentX(Component.LEFT_ALIGNMENT);
//                    pContainer.add(label);
//                    DefaultListModel smellOpts = new DefaultListModel();
//                    smellOpts.addElement("Long Parameter List");
//                    smellOpts.addElement("Multifaceted Abstraction");
//                    smellOpts.addElement("Complex Method");
//
//                   JList<String> pList = new com.intellij.ui.components.JBList(smellOpts);
//                   pList.setOpaque(true);
//
//                    pList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
//                    pList.setAlignmentX(Component.LEFT_ALIGNMENT);
//                    pContainer.add(pList);
//
//                    JOptionPane.showMessageDialog(null,new JScrollPane(pContainer),"smells",JOptionPane.INFORMATION_MESSAGE);
                } else
                    infoMessage += "<p style='width: 400px;'><b>" + smellList.get(i) + " was identified as " + smellBol.get(i) + ". </b> " + "</p>";
            }
            infoMessage += "Are these results accurate? </b></body></html>";
            int result = JOptionPane.showOptionDialog(
                    null, infoMessage, "Smells", JOptionPane.YES_NO_OPTION
                    , JOptionPane.QUESTION_MESSAGE,
                    IconLoader.getIcon("Images/tagcoder_logo.png"),
                    options,
                    options[0]);
            //null, infoMessage, "DesigniteJava: Detected smells", JOptionPane.YES_NO_OPTION, IconLoader.getIcon("Images/designite_logo.png", LineMarker.class),options,options[0]);
            if (result == JOptionPane.YES_OPTION) {
                smellList.forEach(smell -> IntelliJ_Utils.sendFeedback(psiElement.getText(), smell, true));
                IntelliJ_Utils.sendFeedback(psiElement.getText(), smellList.get(0), true);


            } else if (result == JOptionPane.NO_OPTION) {
                if (smellList.stream().anyMatch(smell -> smell.equalsIgnoreCase("no smell"))) {
                    String[] smellOpts = {
                            "Long Parameter List", "Multifaceted Abstraction", "Complex Method"};

                    JOptionPane.showInputDialog(null, "Please choose smells discovered","Select Smells",JOptionPane.QUESTION_MESSAGE,null, smellOpts, smellOpts[0]);
                }
                IntelliJ_Utils.sendFeedback(psiElement.getText(), smellList.get(0), false);
            } else {
                System.out.println("Cancelled");
            }
        }
    }
}