package com.kazurayam.materialstore.store.reporter

import com.kazurayam.materialstore.store.DiffArtifact
import com.kazurayam.materialstore.store.DiffArtifacts
import com.kazurayam.materialstore.store.DiffReporter
import com.kazurayam.materialstore.store.JobName
import com.kazurayam.materialstore.store.Material
import com.kazurayam.materialstore.store.differ.DifferUtil
import groovy.xml.MarkupBuilder
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.nio.file.Files
import java.nio.file.Path

import groovy.json.JsonOutput

class DiffArtifactsBasicReporter implements DiffReporter {

    private static final Logger logger = LoggerFactory.getLogger(DiffArtifactsBasicReporter.class)

    private final Path root_

    private final JobName jobName_

    private Double criteria_ = 0.0d

    DiffArtifactsBasicReporter(Path root, JobName jobName) {
        Objects.requireNonNull(root)
        Objects.requireNonNull(jobName)
        if (! Files.exists(root)) {
            throw new IllegalArgumentException("${root} is not present")
        }
        this.root_ = root
        this.jobName_ = jobName
    }

    @Override
    void setCriteria(Double criteria) {
        if (criteria < 0.0 || 100.0 < criteria) {
            throw new IllegalArgumentException("criteria(${criteria}) must be in the range of [0,100)")
        }
        this.criteria = criteria
    }

    @Override
    int reportDiffs(DiffArtifacts diffArtifacts, String reportFileName) {
        Objects.requireNonNull(diffArtifacts)
        Objects.requireNonNull(reportFileName)
        //
        Path reportFile = root_.resolve(reportFileName)
        //
        int warnCount = 0
        //
        StringWriter sw = new StringWriter()
        MarkupBuilder mb = new MarkupBuilder(sw)
        mb.html(lang: "en") {
            head() {
                meta(charset: "utf-8")
                meta(name: "viewport", content: "width=device-width, initial-scale=1")
                mkp.comment("Bootstrap")
                link(href: "https://cdn.jsdelivr.net/npm/bootstrap@5.0.0-beta1/dist/css/bootstrap.min.css",
                        rel: "stylesheet",
                        integrity: "sha384-giJF6kkoqNQ00vy+HMDP7azOuL0xtbfIcaT9wjKHr8RbDVddVHyTfAAsrekwKmP1",
                        crossorigin: "anonymous")
                style(getStyle())
                title(jobName_.toString())
            }
            body() {
                div(class: "container") {
                    h1(jobName_.toString())
                    div(class: "accordion",
                            id: "diff-contents") {
                        diffArtifacts.eachWithIndex { DiffArtifact da, int index ->
                            div(id: "accordion${index+1}",
                                    class: "accordion-item") {
                                h2(id: "heading${index+1}",
                                        class: "accordion-header") {
                                    button(class: "accordion-button",
                                            type: "button",
                                            "data-bs-toggle": "collapse",
                                            "data-bs-target": "#collapse${index+1}",
                                            "area-expanded": "false",
                                            "aria-controls": "collapse${index+1}") {

                                        Double diffRatio = da.getDiffRatio()
                                        Boolean toBeWarned = decideToBeWarned(diffRatio, criteria_)
                                        if (toBeWarned) {
                                            warnCount += 1
                                        }

                                        String warningClass = getWarningClass(toBeWarned)
                                        span(class: "description ${warningClass}",
                                                da.getDescription())
                                        span(class: "fileType ${warningClass}",
                                                da.getRight().getIndexEntry().getFileType().getExtension())
                                        span(class: "ratio ${warningClass}",
                                                "${DifferUtil.formatDiffRatioAsString(diffRatio)}%")
                                    }
                                }
                                div(id: "collapse${index+1}",
                                        class: "according-collapse collapse",
                                        "aria-labelledby": "heading${index+1}",
                                        "data-bs-parent": "#diff-contents"
                                ) {
                                    mb.div(class: "accordion-body") {
                                        makeModalSubsection(mb, da, index+1)
                                        makeMaterialSubsection(mb, "left", da.getLeft())
                                        makeMaterialSubsection(mb, "right", da.getRight())
                                        makeMaterialSubsection(mb, "diff", da.getDiff())
                                    }
                                }
                            }
                        }
                    }
                }
                mkp.comment("Bootstrap")
                script(src: "https://cdn.jsdelivr.net/npm/bootstrap@5.0.0-beta1/dist/js/bootstrap.bundle.min.js",
                        integrity: "sha384-ygbV9kiqUc6oa4msXn9868pTtWMgiQaeYH7/t7LECLbyPA2x65Kgf80OJFdroafW",
                        crossorigin: "anonymous", "")
            }
        }
        reportFile.toFile().text = "<!doctype html>\n" + sw.toString()

        return warnCount
    }

    private static void makeModalSubsection(MarkupBuilder mb, DiffArtifact da, Integer seq) {
        Material right = da.getRight()
        mb.div(class: "show-diff") {
            if (right.isImage()) {
                String imageModalId = "imageModal${seq}"
                String imageModalTitleId = "imageModalLabel${seq}"
                String carouselId = "carouselControl${seq}"
                // Show 3 images in a Modal
                mkp.comment("Button trigger modal")
                button(type: "button", class: "btn btn-primary",
                        "data-bs-toggle": "modal",
                        "data-bs-target": "#${imageModalId}",
                        "Show diff in Modal")
                mkp.comment("Modal to show 3 images: Left/Diff/Right")
                div(class: "modal fade",
                        id:"${imageModalId}",
                        tabindex: "-1",
                        "aria-labelledby": "imageModalLabel", "aria-hidden": "true") {
                    div(class: "modal-dialog modal-fullscreen"){
                        div(class: "modal-content") {
                            div(class: "modal-header") {
                                h5(class: "modal-title",
                                        id: "${imageModalTitleId}") {
                                    span("${da.getDescriptor()} ${da.getFileTypeExtension()} ${da.getDiffRatioAsString()}%")
                                    button(type: "button",
                                            class: "btn-close",
                                            "data-bs-dismiss": "modal",
                                            "aria-label": "Close",
                                            "")
                                }
                            }
                            div(class: "modal-body") {
                                mkp.comment("body")
                                div(id: "${carouselId}",
                                        class: "carousel slide",
                                        "data-bs-ride": "carousel") {
                                    div(class: "carousel-inner") {
                                        div(class: "carousel-item") {
                                            h3(class: "centered","Left")
                                            img(class: "img-fluid d-block w-75 centered",
                                                    alt: "left",
                                                    src: da.getLeft()
                                                            .getRelativeURL())
                                        }
                                        div(class: "carousel-item active") {
                                            h3(class: "centered","Diff")
                                            img(class: "img-fluid d-block w-75 centered",
                                                    alt: "diff",
                                                    src: da.getDiff()
                                                            .getRelativeURL())
                                        }
                                        div(class: "carousel-item") {
                                            h3(class: "centered","right")
                                            img(class: "img-fluid d-block w-75 centered",
                                                    alt: "right",
                                                    src: da.getRight()
                                                            .getRelativeURL())
                                        }
                                    }
                                    button(class: "carousel-control-prev",
                                            type: "button",
                                            "data-bs-target": "#${carouselId}",
                                            "data-bs-slide": "prev") {
                                        span(class: "carousel-control-prev-icon",
                                                "aria-hidden": "true","")
                                        span(class: "visually-hidden",
                                                "Previous")
                                    }
                                    button(class: "carousel-control-next",
                                            type: "button",
                                            "data-bs-target": "#${carouselId}",
                                            "data-bs-slide": "next") {
                                        span(class: "carousel-control-next-icon",
                                                "aria-hidden": "true","")
                                        span(class: "visually-hidden",
                                                "Next")
                                    }
                                }
                            }
                            div(class: "modal-footer") {
                                button(type: "button", class: "btn btn-secondary",
                                        "data-bs-dismiss": "modal", "Close")
                            }
                        }
                    }
                }
            } else if (right.isText()) {
                String textModalId = "textModal${seq}"
                String textModalTitleId = "textModalLabel${seq}"
                mkp.comment("Button trigger modal")
                button(type: "button", class: "btn btn-primary",
                        "data-bs-toggle": "modal",
                        "data-bs-target": "#${textModalId}",
                        "Show diff in Modal")
                mkp.comment("Modal to show texts diff")
                div(class: "modal fade",
                        id: "${textModalId}",
                        tabindex: "-1",
                        "aria-labelledby": "textModalLabel", "aria-hidden": "true") {
                    div(class: "modal-dialog modal-fullscreen") {
                        div(class: "modal-content") {
                            div(class: "modal-header") {
                                h5(class: "modal-title",
                                        id: "${textModalTitleId}") {
                                    span("${da.getDescriptor()} ${da.getFileTypeExtension()} ${da.getDiffRatioAsString()}%")
                                    button(type: "button",
                                            class: "btn-close",
                                            "data-bs-dismiss": "modal",
                                            "aria-label": "Close",
                                            "")
                                }
                            }
                            div(class: "modal-body") {
                                mkp.comment("body")
                                iframe(src: da.getDiff().getRelativeURL(),
                                        title: "TextDiff", "")
                            }
                            div(class: "modal-footer") {
                                button(type: "button",
                                        class: "btn btn-secondary",
                                        "data-bs-dismiss": "modal",
                                        "Close")
                            }
                        }
                    }
                }
            } else {
                logger.warn("material.isImage() returned false and material.isText() returned false. What is this? ${material}")
            }
        }
    }

    private static void makeMaterialSubsection(MarkupBuilder mb, String name, Material material) {
        mb.div(class: "show-detail") {
            h2(name)
            dl(class: "detail") {
                dt("URL")
                dd() {
                    a(href: material.getRelativeURL(),
                            target: name,
                            material.getRelativeURL())
                }
                //
                dt("fileType")
                dd(material.getIndexEntry().getFileType().getExtension())
                //
                String s = JsonOutput.prettyPrint(material.getIndexEntry().getMetadata().toString())
                dt("metadata")
                dd(s)
            }
        }
    }

    static Boolean decideToBeWarned(Double diffRatio, Double criteria) {
        return diffRatio > criteria
    }

    static String getWarningClass(boolean toBeWarned) {
        if (toBeWarned) {
            return "warning"
        } else {
            return ""
        }
    }

    private static String getStyle() {
        return """
.centered {
    display: block;
    margin-left: auto;
    margin-right: auto;
    text-align: center;
}
.carousel-inner {
    background-color: #efefef;
}
.carousel-control-prev, .carousel-control-next {
    width: 12.5%
}
.modal-body iframe {
    position: absolute;
    border: none;
    height: 100%;
    width: 100%
}

body {
    font-family: ui-monospace, SFMono-Regular,SZ Mono, Menlo, Consolas,Liberation Mono, monospace;
    font-size: 12px;
    line-height: 20px;
}
.show-detail {
    margin-top: 10px;
    margin-bottom: 40px;
}
dl dd {
    margin-left: 40px;
}
.description, .fileType, .ratio {
    padding-top: 4px;
    padding-right: 20px;
    padding-bottom: 4px;
    padding-left: 4px;
    text-align: left;
}
.description {
    flex-basis: 80%;
}
.filetype {
    flex-basis: 10%;
}
.ratio {
    flex-basis: 10%;
}
.warning {
    background-color: #e0ae00;
}
"""
    }
}