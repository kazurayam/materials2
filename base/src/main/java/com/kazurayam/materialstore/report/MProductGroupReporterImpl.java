package com.kazurayam.materialstore.report;

import com.kazurayam.materialstore.filesystem.MaterialstoreException;
import com.kazurayam.materialstore.filesystem.Store;
import com.kazurayam.materialstore.filesystem.metadata.SortKeys;
import com.kazurayam.materialstore.reduce.MProductGroup;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.parser.Parser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * MProductGroupReporterImplMB re-implemented using FreeMarker.
 *
 */
public final class MProductGroupReporterImpl extends MProductGroupReporter {

    private static final Logger logger =
            LoggerFactory.getLogger(MProductGroupReporterImpl.class);

    private final Store store;

    private Double criteria = 0.0d;

    private static final String TEMPLATE_PATH =
            "com/kazurayam/materialstore/report/MProductGroupBasicReporterFMTemplate.ftlh";
    // ftlh is a short for "FreeMarker Template Language for HTML"

    private final Configuration cfg;

    public MProductGroupReporterImpl(Store store) throws MaterialstoreException {
        Objects.requireNonNull(store);
        this.store = store;
        this.cfg = FreeMarkerConfigurator.configureFreeMarker(store);
    }

    @Override
    public void setCriteria(Double criteria) {
        if (criteria < 0.0 || 100.0 < criteria) {
            throw new IllegalArgumentException(
                    "criteria(${criteria}) must be in the range of [0,100)");
        }
        this.criteria = criteria;
    }

    @Override
    public Path report(MProductGroup mProductGroup, String fileName)
            throws MaterialstoreException {
        return this.report(mProductGroup, new SortKeys(), fileName);
    }

    @Override
    public Path report(MProductGroup mProductGroup, SortKeys sortKeys, String fileName)
            throws MaterialstoreException {
        mProductGroup.setCriteria(this.criteria);
        Path reportFile = store.getRoot().resolve(fileName);
        this.report(mProductGroup, sortKeys, reportFile);
        return reportFile;
    }

    @Override
    public void report(MProductGroup mProductGroup, Path filePath)
            throws MaterialstoreException {
        this.report(mProductGroup, new SortKeys(), filePath);
    }
    @Override
    public void report(MProductGroup mProductGroup, SortKeys sortKeys, Path filePath)
            throws MaterialstoreException {
        Objects.requireNonNull(mProductGroup);
        Objects.requireNonNull(sortKeys);
        Objects.requireNonNull(filePath);
        if (! mProductGroup.isReadyToReport()) {
            throw new MaterialstoreException(
                    "given MProductGroup is not ready to report. mProductGroup=" +
                            mProductGroup.toString());
        }
        /* sort the entries in the mProductGroup as specified by SortKeys */
        mProductGroup.order(sortKeys);

        /* create a data-model */
        Map<String, Object> model = new HashMap<>();
        model.put("style", StyleHelper.loadStyleFromClasspath());
        model.put("accordionCustom",
                StyleHelper.loadStyleFromClasspath("/com/kazurayam/materialstore/report/bootstrap-5-accordion-with-an-inline-checkbox.css"));
        model.put("js",
                StyleHelper.loadStyleFromClasspath("/com/kazurayam/materialstore/report/model-manager.js"));
        model.put("title", getTitle(filePath));
        model.put("store", store.getRoot().normalize().toString());
        model.put("mProductGroup", mProductGroup.toTemplateModel());
        model.put("model", mProductGroup.toJson(true));
        model.put("criteria", criteria);
        model.put("sortKeys", sortKeys.toString());

        // for debug
        if (isVerboseLoggingEnabled()) {
            writeModel(mProductGroup.toTemplateModelAsJson(true),
                    filePath.getParent());
        }

        /* Get the template */
        Template template;
        try {
           template = cfg.getTemplate(TEMPLATE_PATH);
        } catch (IOException e) {
            throw new MaterialstoreException(e);
        }

        /* Merge data-model with template to generate a HTML document*/
        Writer sw = new StringWriter();
        try {
            template.process(model, sw);
            sw.flush();
            sw.close();
        } catch (IOException | TemplateException e) {
            throw new MaterialstoreException(e);
        }

        String html;

        /* pretty print the HTML using jsoup if required */
        if (isPrettyPrintingEnabled()) {
            Document doc = Jsoup.parse(sw.toString(), "", Parser.htmlParser());
            doc.outputSettings().indentAmount(2);
            html = doc.toString();
        } else {
            html = sw.toString();
        }

        try {
            Files.write(filePath,
                    html.getBytes(StandardCharsets.UTF_8),
                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            throw new MaterialstoreException(e);
        }
    }
}
