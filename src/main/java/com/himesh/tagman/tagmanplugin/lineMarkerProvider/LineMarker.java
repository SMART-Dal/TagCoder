package com.himesh.tagman.tagmanplugin.lineMarkerProvider;

import com.himesh.tagman.tagmanplugin.assetLoaders.DesigniteAssetLoader;
import com.himesh.tagman.tagmanplugin.logger.DesigniteLogger;
import com.himesh.tagman.tagmanplugin.models.CodeSmell;
import com.himesh.tagman.tagmanplugin.models.DesignSmell;
import com.himesh.tagman.tagmanplugin.models.ImplementationSmell;
import com.intellij.codeHighlighting.Pass;
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
import com.intellij.util.FunctionUtil;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class LineMarker extends RelatedItemLineMarkerProvider {
    DesigniteAssetLoader designiteAssets = new DesigniteAssetLoader();
    //ProjectSmellsInfo smellsInfoProvider = null;

    protected void collectNavigationMarkers(@NotNull PsiElement element,
                                            @NotNull Collection<? super RelatedItemLineMarkerInfo<?>> result) {
        //DesigniteLogger.getLogger().log(Level.INFO, "Invoked.");
        ProjectSmellsInfo smellsInfoProvider = SmellsInfoProvider.getInstance(element.getProject());
        //if (smellsInfoProvider.getProject() != null)
        //    DesigniteLogger.getLogger().log(Level.INFO, "fetching info for " + smellsInfoProvider.getProject().getName());

        if (element instanceof PsiClass) {
            PsiClass psiClass = (PsiClass) element;
            List<DesignSmell> designSmellList = smellsInfoProvider.getDesignSmellList();
            if (psiClass.getQualifiedName() == null)
                return;
            List<CodeSmell> filteredList = designSmellList
                    .stream()
                    .filter(smell -> psiClass.getQualifiedName().equals(smell.getPkg() + "." + smell.getClassName()))
                    .collect(Collectors.toList());
            if (filteredList.size() > 0) {
                System.out.println("psitoken"+psiClass.getContainingFile().getText());
                MyLineMarkerInfo info = new MyLineMarkerInfo(element, designiteAssets,
                        filteredList, "design");
                result.add(info);
            }
        }

        if (element instanceof PsiMethod) {
            List<ImplementationSmell> implementationSmellList = smellsInfoProvider.getImplementationSmellsList();
            PsiMethod psiMethod = (PsiMethod) element;
            String currentClass = Objects.requireNonNull(psiMethod.getContainingClass()).getQualifiedName();
            if (currentClass == null)
                return;
            try {
                List<CodeSmell> filteredList = implementationSmellList
                        .stream()
                        .filter(smell -> currentClass.equals(smell.getPkg() + "." + smell.getClassName()) &&
                                psiMethod.getName().equals(smell.getMethodName()))
                        .collect(Collectors.toList());

                if (filteredList.size() > 0) {
                    System.out.println("psitoken"+psiMethod.getContainingFile().getText());
                    MyLineMarkerInfo info = new MyLineMarkerInfo(element, designiteAssets,
                            filteredList, "implementation");
                    result.add(info);
                }
            } catch (Exception ex) {
                DesigniteLogger.getLogger().log(Level.WARNING, ex.getMessage());
            }
        }
    }

    private static class MyLineMarkerInfo extends RelatedItemLineMarkerInfo<PsiElement> {
        private final List<CodeSmell> smellList;

        private MyLineMarkerInfo(@NotNull PsiElement element,
                                 DesigniteAssetLoader designiteAssetLoader,
                                 List<CodeSmell> smellList,
                                 String smellType) {
            super(element,
                    element.getTextRange(),
                    designiteAssetLoader.getDesigniteIcon(),
                    Pass.LOCAL_INSPECTIONS,
                    FunctionUtil.constant(smellList.size() + " " + smellType + " smell(s) detected."),
                    null,
                    GutterIconRenderer.Alignment.RIGHT,
                    NotNullLazyValue.createConstantValue(Collections.emptyList())
            );
            this.smellList = smellList;
        }

        @Override
        public GutterIconRenderer createGutterRenderer() {
            if (myIcon == null) return null;
            return new LineMarkerGutterIconRenderer<PsiElement>(this) {
                @Override
                public AnAction getClickAction() {
                    return new SimplePopDialogAction(smellList);
                }
            };
        }
    }

    public static class SimplePopDialogAction extends AnAction {
        private final List<CodeSmell> smellList;

        public SimplePopDialogAction(List<CodeSmell> smellList) {
            this.smellList = smellList;
        }

        @Override
        public void actionPerformed(@NotNull AnActionEvent e) {
            String infoMessage = "<html><body>";
            for (CodeSmell smell : smellList) {
                infoMessage += "<p style='width: 400px;'><b>" + smell.getName() + "</b>: " +
                        smell.getDescription().replace(';', ' ') + "</p>";
            }
            infoMessage += "</body></html>";
            JOptionPane.showMessageDialog(null, infoMessage, "DesigniteJava: Detected smells",
                    JOptionPane.WARNING_MESSAGE,
                    IconLoader.getIcon("Images/designite_logo.png", LineMarker.class));
        }
    }
}


