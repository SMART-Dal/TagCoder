package com.himesh.tagman.tagmanplugin;

import com.himesh.tagman.tagmanplugin.assetLoaders.DesigniteAssetLoader;
import com.himesh.tagman.tagmanplugin.constants.Constants;
import com.himesh.tagman.tagmanplugin.filesManager.AppProperties;
import com.himesh.tagman.tagmanplugin.lineMarkerProvider.ProjectSmellsInfo;
import com.himesh.tagman.tagmanplugin.lineMarkerProvider.SmellsInfoProvider;
import com.himesh.tagman.tagmanplugin.logger.DesigniteLogger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.StartupActivity;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.HttpClients;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.InputStream;
import java.io.Serializable;
import java.nio.file.Path;
import java.util.logging.Level;
import org.apache.http.impl.client.*;

public class TagmanProjectListner implements StartupActivity {

//    public void projectClosed() {
//        ToolWindowFactory.toolWindow = null;
//        //SmellsInfoProvider.resetInstance();
//    }

    @Override
    public void runActivity(@NotNull Project project) {

    System.out.println("Console Log.");
    
    }

    private void setDefaultSettingsForPlugin() {
        AppProperties<Serializable> designiteProperties = new AppProperties<>();
        File settingsFilePath = new File(designiteProperties.getSettingFilePath());
        if (!settingsFilePath.exists()) {
            // Writing default values
            designiteProperties.writeProperty(Constants.ANALYZE_ON_STARTUP, "true");
            designiteProperties.writeProperty(Constants.DESIGNITE_JAR_FILE_PATH, "");
            designiteProperties.writeProperty(Constants.XMX_VALUE, "");
            designiteProperties.writeProperty(Constants.LICENSE_KEY, "");
            designiteProperties.buildProperties();
       }
    }

    public void invokeDesignite(Project project) {
        String openedProjectLocation = project.getBasePath();
        DesigniteAssetLoader designiteAssetLoader = new DesigniteAssetLoader();
        ProjectSmellsInfo designiteFileLoader = SmellsInfoProvider.getInstance(project);
        System.out.println("output folder"+designiteFileLoader.getDesigniteOutputFolder());
        System.out.println("designite folder"+designiteAssetLoader.designiteJarFileLocation());

        try {
            DesigniteLogger.getLogger().log(Level.INFO, "Designite Analyzing ...");
    ProcessBuilder processBuilder = new ProcessBuilder("java ","-jar", designiteAssetLoader.designiteJarFileLocation(),
                    "-i", openedProjectLocation, "-o", designiteFileLoader.getDesigniteOutputFolder());
            //processBuilder.redirectOutput(temp);
            Process process = processBuilder.start();
            process.waitFor();

            InputStream inputMessage = process.getInputStream();
            InputStream errorMessage = process.getErrorStream();

            byte[] inputBytes = new byte[inputMessage.available()];
            inputMessage.read(inputBytes, 0, inputBytes.length);
            if (inputBytes.length > 0)
                DesigniteLogger.getLogger().log(Level.INFO, "Designite results ...\n" + new String(inputBytes));

            byte[] errorBytes = new byte[errorMessage.available()];
            errorMessage.read(errorBytes, 0, errorBytes.length);
            if (errorBytes.length > 0)
                DesigniteLogger.getLogger().log(Level.WARNING, "error: " + new String(errorBytes));
            String stubsApiBaseUri = "http://localhost:8031/api/load-data-files/";
            HttpClient client = HttpClients.createDefault();



            String filePath = designiteFileLoader.getDesigniteOutputFolder()+ File.separatorChar +"MethodMetrics.csv";
            File methodFile = new File(filePath);
            File classFile = new File(designiteFileLoader.getDesigniteOutputFolder()+ File.separatorChar+"TypeMetrics.csv");
            HttpEntity entity = MultipartEntityBuilder.create().addPart("methodFile", new FileBody(methodFile)).addPart("classFile",new FileBody(classFile)).build();
            HttpPost postStubMethod = new HttpPost(stubsApiBaseUri);
            postStubMethod.setEntity(entity);

           // HttpPost postClassStubMethod = new HttpPost(stubsApiBaseUri);
            postStubMethod.setEntity(entity);


            HttpResponse getStubResponse = client.execute(postStubMethod);
            System.out.println("Response"+getStubResponse);

            SmellsInfoProvider.resetInstance(project);

        } catch (Exception e) {
            DesigniteLogger.getLogger().log(Level.WARNING, "Exception occurred: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
