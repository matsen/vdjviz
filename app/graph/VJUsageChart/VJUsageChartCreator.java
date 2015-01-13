package graph.VJUsageChart;

import com.antigenomics.vdjtools.basic.SegmentUsage;
import com.antigenomics.vdjtools.sample.Sample;
import com.antigenomics.vdjtools.sample.SampleCollection;
import models.Account;
import models.UserFile;
import play.Logger;
import play.libs.Json;
import utils.CacheType.CacheType;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class VJUsageChartCreator {
    private UserFile file;
    private Account account;
    private SampleCollection sampleCollection;
    private Sample sample;
    private VJUsageChart vjUsageChart;
    private String cacheName = CacheType.vjUsage.getCacheFileName();
    private boolean created;


    public VJUsageChartCreator(UserFile file, Account account, SampleCollection sampleCollection) {
        this.file = file;
        this.account = account;
        this.sampleCollection = sampleCollection;
        this.sample = sampleCollection.getAt(0);
        this.created = false;
    }

    public VJUsageChartCreator create() {
        SegmentUsage segmentUsage = new SegmentUsage(sampleCollection, false);
        segmentUsage.vUsageHeader();
        segmentUsage.jUsageHeader();
        String sampleId = sample.getSampleMetadata().getSampleId();
        double[][] vjMatrix = segmentUsage.vjUsageMatrix(sampleId);
        List<String> labels = new ArrayList<>();
        String[] vVector = segmentUsage.vUsageHeader();
        String[] jVector = segmentUsage.jUsageHeader();

        double[][] matrix = new double[vVector.length + jVector.length][];
        for (int i = 0; i < vVector.length + jVector.length; i++) {
            matrix[i] = new double[vVector.length + jVector.length];
        }
        for (int i = 0; i < jVector.length; i++) {
            for (int j = 0; j < vVector.length; j++) {
                matrix[i][j + jVector.length] = vjMatrix[i][j];
                matrix[j + jVector.length][i] = vjMatrix[i][j];
            }
        }

        Collections.addAll(labels, jVector);
        Collections.addAll(labels, vVector);
        vjUsageChart = new VJUsageChart(matrix, labels);
        created = true;
        return this;
    }

    public void saveCache() throws Exception {
        if (created) {
            try {
                File cache = new File(file.getDirectoryPath() + "/" + cacheName + ".cache");
                PrintWriter fileWriter = new PrintWriter(cache.getAbsoluteFile());
                fileWriter.write(Json.stringify(Json.toJson(vjUsageChart)));
                fileWriter.close();
            } catch (FileNotFoundException fnfe) {
                Logger.of("user." + account.getUserName()).error("User " + account.getUserName() +
                        ": save cache error [" + file.getFileName() + "," + cacheName + "]");
                fnfe.printStackTrace();
            }
        } else {
            throw new Exception("You should create graph");
        }
    }

}