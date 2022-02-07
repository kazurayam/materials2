package com.kazurayam.materialstore

/**
 * Diff-ability
 *
 * MimeType "text/html" is a simple text so it is diffable by TextDifferToHTML
 * MimeType "image/png" is a image so it is diffable by ImageDifferToPNG
 * MimeType "font/woff2" is a unknown so it is unable to create a diff info.
 */
enum Diffability {

    AS_TEXT,
    AS_IMAGE,
    UNABLE;

}