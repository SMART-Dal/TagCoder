package com.himesh.tagman.tagmanplugin.utils;

import com.himesh.tagman.tagmanplugin.logger.DesigniteLogger;
import com.intellij.ide.DataManager;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.wm.WindowManager;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.HttpClients;

import java.awt.*;
import java.io.File;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;

public class IntelliJ_Utils {
    /*public static Project getActiveProject() {
        Project[] projects = ProjectManager.getInstance().getOpenProjects();
        for (Project project : projects) {
            Window window = WindowManager.getInstance().suggestParentWindow(project);
            if (window != null && window.isActive()) {
                return project;
            }
        }
        return null;
    }*/

    //from which project, this code (plugin) is running
    public static Project getCurrentProject() {
        try {
            DesigniteLogger.getLogger().log(Level.INFO, "Invoked.");
            DataContext dataContext = DataManager.getInstance().getDataContextFromFocusAsync().blockingGet(2000);
            if (dataContext != null) {
                Project project = dataContext.getData(DataKeys.PROJECT);
                if (project != null)
                    DesigniteLogger.getLogger().log(Level.INFO, "Complete... " + project.getName());
                return project;
            }
            DesigniteLogger.getLogger().log(Level.INFO, "Complete... ");
            return null;
        } catch (TimeoutException | ExecutionException toe) {
            return null;
        }
    }

    public static void sendFeedback(String file, String smell, boolean isSmell){
        String stubsApiBaseUri = "http://localhost:8031/api/collect-feedback/";
       try {
           HttpClient client = HttpClients.createDefault();


           URIBuilder builder = new URIBuilder(stubsApiBaseUri);
           builder.addParameter("file", file);
           builder.addParameter("smell", smell);
           builder.addParameter("isSmell", String.valueOf(isSmell));

           String listStubsUri = builder.build().toString();
           HttpPost getStubMethod = new HttpPost(listStubsUri);
           HttpResponse getStubResponse = client.execute(getStubMethod);
           System.out.println(getStubResponse.getStatusLine().getStatusCode());
       }
       catch (Exception e)
       {
           e.printStackTrace();
       }
        //HttpResponse getStubResponse = client.execute(postStubMethod);
    }
}

