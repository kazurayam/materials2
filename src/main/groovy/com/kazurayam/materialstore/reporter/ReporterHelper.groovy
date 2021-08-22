package com.kazurayam.materialstore.reporter

class ReporterHelper {

    private ReporterHelper() {}

    static String getStyle() {
        return """
/* .container */
.container {
    font-family: ui-monospace, SFMono-Regular,SZ Mono, Menlo, Consolas,Liberation Mono, monospace;
    font-size: 12px;
    line-height: 20px;
}
.container dl dd {
    margin-left: 40px;
}

/* .title */
.container .title button {
    float: right;
    margin-top: 6px;
}

/* .header */
.container .header {
    padding: 16px 20px;
}

/* .accordion */
.container .accordion button span {
    font-size: 18px;
    line-height: 26px;
}
.container .accordion button .filetype {
    flex-basis: 10%;
}
.container .accordion button .metadata {
    flex-basis: 80%;
}
.container .accordion button .metadata, .fileType {
    padding-top: 4px;
    padding-right: 20px;
    padding-bottom: 4px;
    padding-left: 4px;
    text-align: left;
}
.container .accordion .show-detail {
    margin-top: 10px;
    margin-bottom: 40px;
}
.container .accordion .accordion-body table {
    table-layout: fixed;
    border-collapse: collapse;
    border-spacing: 0;
    border: 1px solid #ccc;
    width: 100%;
}
.container .accordion .accordion-body td, th {
    font-size: 12px;
    border-right: 1px solid #ccc;
    display: table-cell;
}
.container .accordion .accordion-body th {
    border-bottom: 1px solid #ccc;
}
.container table .blob-code-inner {
    word-wrap: break-word;
    white-space: pre-wrap;
}
.container table .code-equal {
    background-color: #ffffff;
}


/*
 */
.container .accordion .modal .carousel .centered {
    display: block;
    margin-left: auto;
    margin-right: auto;
    text-align: center;
}
.container .accordion .modal .carousel-inner {
    background-color: #efefef;
}
.container .accordion .modal .carousel-control-prev, .carousel-control-next {
    width: 12.5%
}
.container .accordion .modal-body iframe {
    position: absolute;
    border: none;
    height: 100%;
    width: 100%
}

.container .accordion .show-detail {
    margin-top: 10px;
    margin-bottom: 40px;
}

.container .accordion button .description, .fileType, .ratio {
    padding-top: 4px;
    padding-right: 4px;
    padding-bottom: 4px;
    padding-left: 4px;
    text-align: left;
}
.container .accordion button .ratio {
    flex-basis: 6%;
    text-align: right;
}
.container .accordion button .filetype {
    flex-basis: 6%;
}
.container .accordion button .warning {
    background-color: #e0ae00;
}
"""
    }
}
