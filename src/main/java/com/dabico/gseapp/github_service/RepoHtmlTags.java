package com.dabico.gseapp.github_service;

public class RepoHtmlTags {
    static String isPageValid         = "#js-repo-pjax-container";
    static String commitsReg          = "#js-repo-pjax-container > div > div > div > div > div > div > div > div:nth-child(4) > ul > li > a > span > strong";
    static String linkTemplateAlt     = "#js-repo-pjax-container > div.container-xl.clearfix.new-discussion-timeline.px-3.px-md-4.px-lg-5 > div > div.gutter-condensed.gutter-lg.d-flex.flex-column.flex-md-row > div.flex-shrink-0.col-12.col-md-3 > div > div:nth-child(%d) > div > div > a";
    static String commitsAlt          = "#js-repo-pjax-container > div.container-xl.clearfix.new-discussion-timeline.px-3.px-md-4.px-lg-5 > div > div.gutter-condensed.gutter-lg.d-flex.flex-column.flex-md-row > div.flex-shrink-0.col-12.col-md-9.mb-4.mb-md-0 > div.Box.mb-3 > div.Box-header.Box-header--blue.position-relative > div > div:nth-child(4) > ul > li > a > span > strong";
    static String branchesReg         = "#js-repo-pjax-container > div > div > div > div > div > div > a:nth-child(1) > strong";
    static String branchesAlt         = "#js-repo-pjax-container > div.container-xl.clearfix.new-discussion-timeline.px-3.px-md-4.px-lg-5 > div > div.gutter-condensed.gutter-lg.d-flex.flex-column.flex-md-row > div.flex-shrink-0.col-12.col-md-9.mb-4.mb-md-0 > div.file-navigation.mb-3.d-flex.flex-items-start > div.flex-self-center.ml-3.flex-self-stretch.d-none.d-lg-flex.flex-items-center.lh-condensed-ultra > a:nth-child(1) > strong";
    static String releasesReg         = "#js-repo-pjax-container > div > div > div > div > div > div > a:nth-child(2) > strong";
    static String releasesAlt         = "#js-repo-pjax-container > div.container-xl.clearfix.new-discussion-timeline.px-3.px-md-4.px-lg-5 > div > div.gutter-condensed.gutter-lg.d-flex.flex-column.flex-md-row > div.flex-shrink-0.col-12.col-md-9.mb-4.mb-md-0 > div.file-navigation.mb-3.d-flex.flex-items-start > div.flex-self-center.ml-3.flex-self-stretch.d-none.d-lg-flex.flex-items-center.lh-condensed-ultra > a:nth-child(2) > strong";
    static String actionListReg       = "#js-repo-pjax-container > div > div > ul";
    static String actionListAlt       = "#js-repo-pjax-container > div.pagehead.repohead.readability-menu.bg-gray-light.pb-0.pt-3.border-0.mb-5 > div.d-flex.mb-3.px-3.px-md-4.px-lg-5 > ul";
    static String watchersTemplateReg = "#js-repo-pjax-container > div > div > ul > li:nth-child(%d) > a:nth-child(2)";
    static String watchersTemplateAlt = "#js-repo-pjax-container > div.pagehead.repohead.hx_repohead.readability-menu.bg-gray-light.pb-0.pt-3 > div > ul > li:nth-child(%d) > a:nth-child(2)";
    static String sidebarReg          = "#js-repo-pjax-container > div.container-xl.clearfix.new-discussion-timeline.px-3.px-md-4.px-lg-5 > div > div.gutter-condensed.gutter-lg.d-flex.flex-column.flex-md-row > div.flex-shrink-0.col-12.col-md-3 > div";
    static String contribTemplateReg  = "#js-repo-pjax-container > div > div > div > div > div > div:nth-child(%d) > div > h2 > a > span";
    static String contribTemplateAlt  = "#js-repo-pjax-container > div.container-xl.clearfix.new-discussion-timeline.px-3.px-md-4.px-lg-5 > div > div.gutter-condensed.gutter-lg.d-flex.flex-column.flex-md-row > div.flex-shrink-0.col-12.col-md-3 > div > div:nth-child(%d) > div > h2 > a > span";
    static String linkTemplateReg     = "#js-repo-pjax-container > div > div > div > div > div > div:nth-child(%d) > div > div > a";
    static String openReg             = "#js-issues-toolbar > div > div > div > a:nth-child(1)";
    static String closedReg           = "#js-issues-toolbar > div > div > div > a:nth-child(2)";
    static String commitDateReg       = "#js-repo-pjax-container > div.container-xl.clearfix.new-discussion-timeline.px-3.px-md-4.px-lg-5 > div > div.js-navigation-container.js-active-navigation-container.mt-3 > div.TimelineItem.TimelineItem--condensed.pt-0.pb-2 > div.TimelineItem-body > ol > li > div.flex-auto.min-width-0 > div.d-flex.flex-items-center.mt-1 > div.f6.text-gray.min-width-0 > relative-time";
    static String commitSHAReg        = "#js-repo-pjax-container > div.container-xl.clearfix.new-discussion-timeline.px-3.px-md-4.px-lg-5 > div > div.js-navigation-container.js-active-navigation-container.mt-3 > div.TimelineItem.TimelineItem--condensed.pt-0.pb-2 > div.TimelineItem-body > ol > li > div.d-none.d-md-block.flex-shrink-0 > div > clipboard-copy";
}
