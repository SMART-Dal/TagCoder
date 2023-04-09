package com.himesh.tagman.tagmanplugin;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.himesh.tagman.tagmanplugin.assetLoaders.DesigniteAssetLoader;
import com.himesh.tagman.tagmanplugin.constants.Constants;
import com.himesh.tagman.tagmanplugin.filesManager.AppProperties;
import com.himesh.tagman.tagmanplugin.filesManager.SettingsFolder;
import com.himesh.tagman.tagmanplugin.logger.DesigniteLogger;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.util.IconLoader;
import org.apache.commons.io.FileUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.List;
import java.util.logging.Level;
import java.util.stream.Collectors;

import static com.himesh.tagman.tagmanplugin.constants.Constants.*;


public class DesigniteMenuRetrain extends AnAction {

    public DesigniteMenuRetrain() {
        super("Retrain Model Now", "Retrain the AutoEncoder now.", null);
//                IconLoader.getIcon("Images/analysis.png")); //It is not looking good
    }

    @Override
    public void actionPerformed(AnActionEvent event) {

        TagmanProjectListner projectAction = new TagmanProjectListner();
        JOptionPane.showMessageDialog(null,
                ANALYSIS_TEXT, "TagCoder - Retrain Model",
                JOptionPane.INFORMATION_MESSAGE,
                IconLoader.getIcon("Images/tagcoder_logo.png", DesigniteMenuRetrain.class));

        if (event != null && event.getProject() != null) {
            try {
                String stubsApiBaseUri = "http://localhost:8031/api/retrain/";
                HttpClient client = HttpClients.createDefault();

                HttpPost getStubMethod = new HttpPost(stubsApiBaseUri);
                HttpResponse getStubResponse = client.execute(getStubMethod);
                int getStubStatusCode = getStubResponse.getStatusLine().getStatusCode();
                if (getStubStatusCode < 200 || getStubStatusCode >= 300) {
                    MyNotifier.notifyError(event.getProject(), "Error retraining the model. Please try again later. If the error persists, please contact hnandani@dal.ca");
                    System.out.println("stub"+getStubStatusCode);
                    return;
                }
                HttpEntity entity = getStubResponse.getEntity();
                if (entity != null) {

                    MyNotifier.notifyInfo(event.getProject(),"Successfully submitted data to retrain model.");
                    // do something with the JSON object
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
