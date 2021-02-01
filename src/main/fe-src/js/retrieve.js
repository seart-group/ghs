function formatBytes(bytes, decimals = 2) {
    if (bytes === 0) return '0 B';
    const k = 1024;
    const dm = (decimals < 0) ? 0 : decimals;
    const sizes = ['B','KB','MB','GB','TB','PB','EB','ZB','YB'];
    const i = Math.floor(Math.log(bytes) / Math.log(k));
    return parseFloat((bytes / Math.pow(k, i)).toFixed(dm))+' '+sizes[i];
}

function appendResult(item) {
    let repo_id = item.id;
    let repo_name = item.name;
    let repo_isFork = item.isFork;
    let repo_commits = (item.commits < 0) ? '?' : item.commits;
    let repo_branches = (item.branches < 0) ? '?' : item.branches;
    let repo_defaultBranch = item.defaultBranch;
    let repo_releases = (item.releases < 0) ? '?' : item.releases;
    let repo_contributors = (item.contributors < 0) ? '?' : item.contributors;
    let repo_license = item.license;
    let repo_watchers = item.watchers;
    let repo_stargazers = item.stargazers;
    let repo_forks = item.forks;
    let repo_size = formatBytes(item.size);
    let repo_createdAt = item.createdAt.replace("T"," ");
    let repo_pushedAt = item.pushedAt.replace("T"," ");
    let repo_updatedAt = item.updatedAt.replace("T"," ");
    let repo_homepage = item.homepage;
    let repo_mainLanguage = item.mainLanguage;
    let repo_totalIssues = item.totalIssues;
    let repo_openIssues = item.openIssues;
    let repo_totalPullRequests = item.totalPullRequests;
    let repo_openPullRequests = item.openPullRequests;
    let repo_lastCommit = (item.lastCommit === null) ? item.lastCommit : item.lastCommit.replace("T"," ");
    let repo_lastCommitSHA = item.lastCommitSHA;
    let repo_hasWiki = item.hasWiki;
    let repo_isArchived = item.isArchived;
    let repo_languages = item.languages.sort((x,y) => (x.sizeOfCode > y.sizeOfCode) ? -1 : 1);
    let repo_labels = item.labels;

    let result = '<div id="repo-'+repo_id+'" class="pt-3"></div>';

    let collapse = '<div id="repo-'+repo_id+'-collapse" class="collapse"></div>';

    let row_1 = '<div class="row mw-100 pt-3 mx-3 border border-bottom-0 bg-lgray"></div>';

    let repo_class_label = 'lang-'+repo_mainLanguage.toLowerCase();
    if(repo_mainLanguage === 'C++')
        repo_class_label = 'lang-cpp';
    else if(repo_mainLanguage === 'C#')
        repo_class_label = 'lang-csharp';

    let row_1_col_1 = '<div class="col d-flex align-items-center justify-content-start"><span class="mx-1 fa-2x '+repo_class_label+'" title="'+repo_mainLanguage+' Repository"></span><a href="https://github.com/'+repo_name+'" target="_blank" class="mx-1 text-dark rowsublabel">'+repo_name+'</a></div>';
    let row_1_col_2 = '<div class="col-auto d-flex align-items-center justify-content-end"></div>';

    if (repo_isArchived) {
        let archive_span = '<span class="mx-1 icon-archive" title="Archived"></span>';
        row_1_col_2 = $(row_1_col_2).append(archive_span);
    }

    if (repo_isFork){
        let fork_span = '<span class="mx-1 icon-repo-forked" title="Fork Project"></span>';
        row_1_col_2 = $(row_1_col_2).append(fork_span);
    }

    if (repo_hasWiki){
        let wiki_span = '<span class="mx-1 icon-book" title="Has Wiki"></span>';
        row_1_col_2 = $(row_1_col_2).append(wiki_span);
    }

    if (repo_homepage !== null && (!repo_homepage.isEmpty())){
        let globe_span = '<a href="'+repo_homepage+'" target="_blank" class="text-dark" style="text-decoration: none"><span class="mx-1 fa fa-home" title="External Homepage"></span></a>';
        row_1_col_2 = $(row_1_col_2).append(globe_span);
    }

    row_1 = $(row_1).append(row_1_col_1,row_1_col_2);
    result = $(result).append(row_1);

    let row_2 = '<div class="row mw-100 pt-3 mx-3 border border-top-0 border-bottom-0 bg-lgray"><div class="col d-flex justify-content-between"><div class="col d-flex align-items-center justify-content-start pl-0"><i class="mx-1 icon-git-commit" title="Commits"></i><span>Commits: '+repo_commits+'</span></div><div class="col d-flex align-items-center justify-content-start"><i class="mx-1 icon-eye" title="Watchers"></i><span>Watchers: '+repo_watchers+'</span></div><div class="col d-flex align-items-center justify-content-start"><i class="mx-1 icon-star" title="Stargazers"></i><span>Stars: '+repo_stargazers+'</span></div><div class="col d-flex align-items-center justify-content-start pr-0"><i class="mx-1 icon-repo-forked" title="Forks"></i><span>Forks: '+repo_forks+'</span></div></div></div>';
    result = $(result).append(row_2);

    let row_3 = '<div class="row mw-100 pt-3 mx-3 border border-top-0 border-bottom-0 bg-lgray"><div class="col d-flex justify-content-between"><div class="col d-flex align-items-center justify-content-start pl-0"><i class="mx-1 icon-git-branch" title="Branches"></i><span>Branches: '+repo_branches+'</span></div><div class="col d-flex align-items-center justify-content-start"><i class="mx-1 icon-organization" title="Contributors"></i><span>Contributors: '+repo_contributors+'</span></div><div class="col d-flex align-items-center justify-content-start"><i class="mx-1 icon-issue-opened" title="Total Issues"></i><span>Total Issues: '+repo_totalIssues+'</span></div><div class="col d-flex align-items-center justify-content-start pr-0"><i class="mx-1 icon-git-pull-request" title="Total Pull Requests"></i><span>Total Pull Reqs: '+repo_totalPullRequests+'</span></div></div></div>';
    result = $(result).append(row_3);

    let row_4 = '<div class="row mw-100 pt-3 mx-3 border border-top-0 border-bottom-0 bg-lgray"><div class="col d-flex justify-content-between"><div class="col d-flex align-items-center justify-content-start pl-0"><i class="mx-1 icon-file-code" title="Total Size"></i><span>Total Size: '+repo_size+'</span></div><div class="col d-flex align-items-center justify-content-start"><i class="mx-1 icon-tag" title="Releases"></i><span>Releases: '+repo_releases+'</span></div><div class="col d-flex align-items-center justify-content-start"><i class="mx-1 icon-issue-opened icon-red" title="Open Issues"></i><span>Open Issues: '+repo_openIssues+'</span></div><div class="col d-flex align-items-center justify-content-start pr-0"><i class="mx-1 icon-git-pull-request icon-red" title="Open Pull Requests"></i><span>Open Pull Reqs: '+repo_openPullRequests+'</span></div></div></div>';
    collapse = $(collapse).append(row_4);

    let row_5 = '<div class="row mw-100 pt-3 mx-3 border border-top-0 border-bottom-0 bg-lgray"><div class="col d-flex justify-content-between"><div class="col d-flex align-items-center justify-content-start pl-0"><i class="mx-1 icon-repo-template" title="Date Created"></i><span>Created: <span title="'+repo_createdAt.slice(0,19)+'"> '+repo_createdAt.slice(0,10)+'</span></span></div><div class="col d-flex align-items-center justify-content-start"><i class="mx-1 icon-pencil" title="Date Updated"></i><span>Updated: <span title="'+repo_updatedAt.slice(0,19)+'"> '+repo_updatedAt.slice(0,10)+'</span></span></div><div class="col d-flex align-items-center justify-content-start"><i class="mx-1 icon-repo-push" title="Date Last Pushed"></i><span>Last Push: <span title="'+repo_pushedAt.slice(0,19)+'"> '+repo_pushedAt.slice(0,10)+'</span></span></div><div class="col d-flex align-items-center justify-content-start pr-0"><i class="mx-1 icon-diff" title="Last Commit Date"></i><span>Last Commit: <span title="'+((repo_lastCommit !== null) ? repo_lastCommit.slice(0,19) : '')+'"> '+((repo_lastCommit !== null) ? repo_lastCommit.slice(0,10) : '?')+'</span></span></div></div></div>';

    collapse = $(collapse).append(row_5);

    if (repo_license !== null){
        let row_6 = '<div class="row mw-100 pt-3 mx-3 border border-top-0 border-bottom-0 bg-lgray"><div class="col d-flex justify-content-start"><div class="col d-flex align-items-center justify-content-start px-0"><i class="mx-1 icon-law" title="License"></i><span>License: '+repo_license+'</span></div></div></div>';
        collapse = $(collapse).append(row_6);
    }

    let row_7 = '';

    if (repo_lastCommitSHA !== null){
        row_7 += '<div class="row mw-100 pt-3 mx-3 border border-top-0 border-bottom-0 bg-lgray"><div class="col d-flex justify-content-start"><div class="col-auto d-flex align-items-center justify-content-start pl-0"><i class="mx-1 fa fa-hashtag" title="Last Commit SHA"></i><span>Last Commit SHA: '+repo_lastCommitSHA+'</span></div></div></div>';
    }

    row_7 += '<div class="row mw-100 pt-3 mx-3 border border-top-0 border-bottom-0 bg-lgray"><div class="col d-flex justify-content-start"><div class="col-auto d-flex align-items-center justify-content-start pl-0"><i class="mx-1 icon-git-branch" title="Default Branch"></i><span>Default Branch: '+repo_defaultBranch+'</span></div></div></div>';
    collapse = $(collapse).append(row_7);

    let row_8 = '<div class="row mw-100 mx-3 border border-top-0 border-bottom-0 bg-lgray"></div>';
    let row_8_col_1 = '<div class="col-12 mr-0 d-flex flex-wrap align-items-center align-content-between justify-content-start"></div>';
    let row_8_col_2 = '<div class="col-12 d-flex flex-wrap align-items-center align-content-between justify-content-start"></div>';

    if (repo_languages.length > 1){
        row_8_col_1 = $(row_8_col_1).append('<span class="mx-1">Languages:</span>');

        let entries = [];
        let sizes = [];
        let percentages = [];

        repo_languages.forEach(lang => {
            entries.push(lang.language);
            sizes.push(lang.sizeOfCode);
        });

        let totalSize = sizes.reduce((x,y) => x + y, 0);

        sizes.forEach(x => {
            let percentage = (x/totalSize * 100).toFixed(2);
            if (percentage !== "0.00"){
                percentages.push(percentage + "%");
            } else {
                percentages.push("< 0.01%")
            }
        });

        for (let i = 0; i < percentages.length; i++){
            row_8_col_2 = $(row_8_col_2).append('<span class="badge badge-dark m-1">'+entries[i]+': '+percentages[i]+'</span>');
        }

        row_8 = $(row_8).addClass("py-3");
        row_8 = $(row_8).addClass("border-bottom-0");
    }

    row_8 = $(row_8).append(row_8_col_1,row_8_col_2);
    collapse = $(collapse).append(row_8);

    let row_9 = '<div class="row mw-100 mx-3 border border-top-0 border-bottom-0 bg-lgray"></div>';
    let row_9_col_1 = '<div class="col-12 mr-0 d-flex flex-wrap align-items-center align-content-between justify-content-start"></div>';
    let row_9_col_2 = '<div class="col-12 d-flex flex-wrap align-items-center align-content-between justify-content-start"></div>';

    if (repo_labels.length > 0){
        row_9_col_1 = $(row_9_col_1).append('<span class="mx-1">Issue Labels:</span>');
        repo_labels.forEach(label => {
            row_9_col_2 = $(row_9_col_2).append('<span class="badge badge-dark m-1">'+label.label+'</span>');
        });
        row_9 = $(row_9).addClass("pb-3");
        row_9 = $(row_9).addClass("border-bottom-0");
    }

    row_9 = $(row_9).append(row_9_col_1,row_9_col_2);
    collapse = $(collapse).append(row_9);
    result = $(result).append(collapse);

    let row_10 = '<div class="row mw-100 pt-3 mx-3 border border-top-0 bg-lgray"><div class="col d-flex flex-wrap align-items-center align-content-between justify-content-start px-0"><button type="button" id="repo-'+repo_id+'-toggle" class="w-100 btn btn-outline-secondary bg-lgray rounded-0 border-0 text-center" data-toggle="collapse" data-target="#repo-'+repo_id+'-collapse" aria-expanded="false" aria-controls="repo-'+repo_id+'-collapse" onclick="toggleButtonText(\'repo-'+repo_id+'-toggle\')">Show Details</button></div></div>';
    result = $(result).append(row_10);

    $results_container_items.append(result);
}

function retrieve(url) {
    gtag('event', 'search-pages');

    fetch(url).then(response => {
        return response.json();
    }).then(data => {
        let items      = data.items;
        let first      = data.first;
        let prev       = data.prev;
        let next       = data.next;
        let last       = data.last;
        let page       = data.page;
        let base       = data.base;
        let csvLink    = data.csvLink;
        let jsonLink   = data.jsonLink;
        let xmlLink    = data.xmlLink;
        let totalItems = data.totalItems;
        let totalPages = data.totalPages;

        if (jsonLink !== null){
            $download_json_button.attr('href_emad',jsonLink);
        } else {
            $download_json_button.addClass('disabled');
        }

        if (xmlLink !== null){
            $download_xml_button.attr('href_emad',xmlLink);
        } else {
            $download_xml_button.addClass('disabled');
        }

        if (csvLink !== null){
            $download_csv_button.attr('href_emad',csvLink);
        } else {
            $download_csv_button.addClass('disabled');
        }

        $results_container_items.empty();
        $page.val('');

        if (totalItems > 0){
            let count = '<div><div class="row mw-100 pt-3 mx-3"><div class="col d-flex align-items-center justify-content-start px-0"><span class="text-secondary">Total Results: <span id="totalResult_value">'+totalItems+'</span></span></div><div class="col d-flex align-items-center justify-content-end px-0"><span class="text-secondary">Page: '+page+' / '+totalPages+'</span></div></div></div>';
            $results_container_items.append(count);
            if (totalPages > 1){
                $page.removeClass('d-none');
                $go_button.removeClass('d-none');
                $go_button.attr('base-url',base);
                $go_button.attr('page-limit',totalPages)
            }
            items.forEach(item => appendResult(item));
        } else {
            $page.addClass("d-none");
            $go_button.addClass("d-none");
            let no_items = '<div><div class="row mw-100 py-3 mx-3"><div class="col d-flex align-items-center justify-content-center px-0"><i class="mx-1 fa fa-frown-o" style="color: #6c757d"></i><span class="text-secondary">No Results</span></div></div></div>';
            $results_container_items.append(no_items);
        }

        $loading_modal.modal("hide");

        if (first !== null){
            $first_button.removeClass('d-none');
            $first_button.attr('onclick','changePage("'+first+'")')
        } else {
            $first_button.addClass('d-none');
        }

        if (prev !== null){
            $prev_button.removeClass('d-none');
            $prev_button.attr('onclick','changePage("'+prev+'")')
        } else {
            $prev_button.addClass('d-none');
        }

        if (next !== null){
            $next_button.removeClass('d-none');
            $next_button.attr('onclick','changePage("'+next+'")')
        } else {
            $next_button.addClass('d-none');
        }

        if (last !== null){
            $last_button.removeClass('d-none');
            $last_button.attr('onclick','changePage("'+last+'")')
        } else {
            $last_button.addClass('d-none');
        }
    }).catch(err => {
        console.log(err);
        let connect_err = '<div><div class="row mw-100 py-3 mx-3"><div class="col d-flex align-items-center justify-content-center px-0"><i class="mx-1 fa fa-frown-o" style="color: #6c757d"></i><span class="text-secondary">Error connecting to DEVINTA server</span></div></div></div>';
        $download_json_button.addClass('disabled');
        $download_xml_button.addClass('disabled');
        $download_csv_button.addClass('disabled');
        $results_container_items.empty();
        $results_container_items.append(connect_err);
    })
}

function jumpToPage(base_url) {
    let page = parseInt($page.val());
    if (isNaN(page)){ return false; }
    let page_limit = parseInt($go_button.attr('page-limit'));
    if (page <= page_limit){
        $loading_modal.modal("show");
        $body_html.animate({ scrollTop: 0 }, 400);
        let total_result_cache = $('#totalResult_value').text();
        retrieve(base_url+'&page='+(page - 1)+'&pageSize=20'+'&totalResult='+total_result_cache);
    } else {
        alert("Invalid page number: "+page+" should be <= "+page_limit);
        // retrieve(base_url+'&page='+(page_limit - 1)+'&pageSize=20');
    }
}

function changePage(url){
    $loading_modal.modal("show");
    retrieve(url);
}

$go_form.submit(function () {
    let base_url = $go_button.attr('base-url');
    jumpToPage(base_url);
    return false;
});

function submitQuery() {

    gtag('event', 'search');

    let name = document.getElementById("name").value;
    let nameEquals = document.getElementById("match").value;
    let language = document.getElementById("language").value;
    let label = document.getElementById("label").value;
    let license = document.getElementById("license").value;

    let commitsMin = document.getElementById("commits-min").value;
    let commitsMax = document.getElementById("commits-max").value;
    let contributorsMin = document.getElementById("contributors-min").value;
    let contributorsMax = document.getElementById("contributors-max").value;
    let issuesMin = document.getElementById("issues-min").value;
    let issuesMax = document.getElementById("issues-max").value;
    let pullsMin = document.getElementById("pulls-min").value;
    let pullsMax = document.getElementById("pulls-max").value;
    let branchesMin = document.getElementById("branches-min").value;
    let branchesMax = document.getElementById("branches-max").value;
    let releasesMin = document.getElementById("releases-min").value;
    let releasesMax = document.getElementById("releases-max").value;
    let starsMin = document.getElementById("stars-min").value;
    let starsMax = document.getElementById("stars-max").value;
    let watchersMin = document.getElementById("watchers-min").value;
    let watchersMax = document.getElementById("watchers-max").value;
    let forksMin = document.getElementById("forks-min").value;
    let forksMax = document.getElementById("forks-max").value;

    let createdMin = document.getElementById("created-min").value;
    let createdMax = document.getElementById("created-max").value;
    let committedMin = document.getElementById("committed-min").value;
    let committedMax = document.getElementById("committed-max").value;

    let onlyForks = document.getElementById("only-forks").checked;
    let excludeForks = document.getElementById("exclude-forks").checked;
    let hasIssues = document.getElementById("has-issues").checked;
    let hasPulls = document.getElementById("has-pulls").checked;
    let hasWiki = document.getElementById("has-wiki").checked;
    let hasLicense = document.getElementById("has-license").checked;

    let search_base = "http://localhost:8080/r/search?";

    let url_params = "";
    url_params += "nameEquals=" + nameEquals;
    url_params += "&onlyForks=" + onlyForks;
    url_params += "&excludeForks=" + excludeForks;
    url_params += "&hasIssues=" + hasIssues;
    url_params += "&hasPulls=" + hasPulls;
    url_params += "&hasWiki=" + hasWiki;
    url_params += "&hasLicense=" + hasLicense;

    if (!name.isEmpty()){ url_params += "&name=" + name; }
    if (!language.isEmpty()){ url_params += "&language=" + encodeURIComponent(language); } //we have to encode C++ and C# cases
    if (!label.isEmpty()){ url_params += "&label=" + encodeURIComponent(label); }
    if (!license.isEmpty()){ url_params += "&license=" + license; }
    if (!commitsMin.isEmpty()){ url_params += "&commitsMin=" + commitsMin; }
    if (!commitsMax.isEmpty()){ url_params += "&commitsMax=" + commitsMax; }
    if (!contributorsMin.isEmpty()){ url_params += "&contributorsMin=" + contributorsMin; }
    if (!contributorsMax.isEmpty()){ url_params += "&contributorsMax=" + contributorsMax; }
    if (!issuesMin.isEmpty()){ url_params += "&issuesMin=" + issuesMin; }
    if (!issuesMax.isEmpty()){ url_params += "&issuesMax=" + issuesMax; }
    if (!pullsMin.isEmpty()){ url_params += "&pullsMin=" + pullsMin; }
    if (!pullsMax.isEmpty()){ url_params += "&pullsMax=" + pullsMax; }
    if (!branchesMin.isEmpty()){ url_params += "&branchesMin=" + branchesMin; }
    if (!branchesMax.isEmpty()){ url_params += "&branchesMax=" + branchesMax; }
    if (!releasesMin.isEmpty()){ url_params += "&releasesMin=" + releasesMin; }
    if (!releasesMax.isEmpty()){ url_params += "&releasesMax=" + releasesMax; }
    if (!starsMin.isEmpty()){ url_params += "&starsMin=" + starsMin; }
    if (!starsMax.isEmpty()){ url_params += "&starsMax=" + starsMax; }
    if (!watchersMin.isEmpty()){ url_params += "&watchersMin=" + watchersMin; }
    if (!watchersMax.isEmpty()){ url_params += "&watchersMax=" + watchersMax; }
    if (!forksMin.isEmpty()){ url_params += "&forksMin=" + forksMin; }
    if (!forksMax.isEmpty()){ url_params += "&forksMax=" + forksMax; }
    if (!createdMin.isEmpty()){ url_params += "&createdMin=" + createdMin; }
    if (!createdMax.isEmpty()){ url_params += "&createdMax=" + createdMax; }
    if (!committedMin.isEmpty()){ url_params += "&committedMin=" + committedMin; }
    if (!committedMax.isEmpty()){ url_params += "&committedMax=" + committedMax; }


    let final_url = search_base + url_params;
    retrieve(final_url);
}

$main_search_form.submit(function () {
    $loading_modal.modal("show");
    submitQuery();
    $body_html.animate({ scrollTop: 0 }, 400);
    $form_container.toggleClass('d-none');
    $results_container.toggleClass('d-none');
    return false;
});

$back_button.click(function () {
    $main_search_form.get(0).reset();
    $form_container.toggleClass('d-none');
    $results_container.toggleClass('d-none');
    $results_container_items.empty();
    $download_json_button.attr('href','');
    $download_json_button.removeClass('disabled');
    $download_xml_button.attr('href','');
    $download_xml_button.removeClass('disabled');
    $download_csv_button.attr('href','');
    $download_csv_button.removeClass('disabled');
    $first_button.attr('onclick','');
    $first_button.addClass('d-none');
    $prev_button.attr('onclick','');
    $prev_button.addClass('d-none');
    $next_button.attr('onclick','');
    $next_button.addClass('d-none');
    $last_button.attr('onclick','');
    $last_button.addClass('d-none');
    $page.addClass('d-none');
    $page.val('');
    $go_button.addClass('d-none');
    $go_button.attr('base-url','');
    $go_button.attr('page-limit','');
});
