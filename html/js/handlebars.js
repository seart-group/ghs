H.registerHelpers(Handlebars);

Handlebars.registerHelper("startcase", _.startCase);

Handlebars.registerHelper("bytes", function (value) {
    const k = 1024;
    const decimals = 2;
    const units = ['B', 'KB', 'MB', 'GB', 'TB', 'PB'];
    const point = decimals ? '.' : '';
    const zeroes = _.repeat("0", decimals);
    if (!value) return `0${point}${zeroes} ${units[0]}`;
    const formatter = new Intl.NumberFormat("en-US", {
        minimumFractionDigits: 0,
        maximumFractionDigits: decimals,
        roundingIncrement: 1,
        useGrouping: false
    })
    const i = Math.floor(Math.log(value) / Math.log(k));
    const f = formatter.format(value / Math.pow(k, i));
    return `${f} ${units[i]}`;
});

// Localizes an integer (eg 100000 => '100,000')
Handlebars.registerHelper("localized", function (value) {
    return value.toLocaleString('en-US', {
        useGrouping: true,
        minimumFractionDigits: 0
    });
});

Handlebars.registerHelper("percentage", function (value) {
    const percentage = value.toFixed(2);
    return percentage !== "0.00" ? `${percentage}%` : "< 0.01%";
});

Handlebars.registerHelper("switch", function(value, options) {
    this._switch_value_ = value;
    this._switch_break_ = false;
    const html = options.fn(this);
    delete this._switch_break_;
    delete this._switch_value_;
    return html;
});

Handlebars.registerHelper("case", function(value) {
    const args = Array.prototype.slice.call(arguments);
    const options = args.pop();
    if (this._switch_break_ || args.indexOf(this._switch_value_) === -1) {
        return '';
    } else {
        if (options.hash.break === true) {
            this._switch_break_ = true;
        }
        return options.fn(this);
    }
});

Handlebars.registerHelper("default", function (options) {
    if (!this._switch_break_) {
        return options.fn(this);
    }
});

Handlebars.registerHelper("truncate", function (value, options) {
    const length = options.hash.length;
    return value.length > length
        ? `${value.substring(0, length)}`
        : value;
});

Handlebars.registerHelper("date", function (value) {
    return value.split("T")[0];
});

Handlebars.registerHelper("between", function (item, options) {
    const min = options.hash.min || Number.MIN_SAFE_INTEGER;
    const max = options.hash.max || Number.MAX_SAFE_INTEGER;
    switch (typeof item) {
        case "number":
            return _.inRange(item, min, max);
        case "string":
            return _.inRange(item.length, min, max);
        case "object":
            return _.inRange(Object.keys(item).length, min, max);
        default:
            return false;
    }

});

Handlebars.registerHelper("devicon", function (language) {
    switch (language) {
        // Plain mappings
        case "Arduino":
        case "C":
        case "Ceylon":
        case "Clojure":
        case "CMake":
        case "Dart":
        case "Elixir":
        case "Elm":
        case "Erlang":
        case "Gradle":
        case "GraphQL":
        case "Groovy":
        case "Handlebars":
        case "Haskell":
        case "Haxe":
        case "Java":
        case "JavaScript":
        case "Julia":
        case "Kotlin":
        case "Lua":
        case "MATLAB":
        case "OCaml":
        case "PHP":
        case "Python":
        case "Ruby":
        case "Rust":
        case "Sass":
        case "Svelte":
        case "Swift":
        case "TypeScript":
            return `devicon-${language.toLowerCase()}-plain`;
        // "Original" mappings
        case "CoffeeScript":
        case "Markdown":
        case "Nginx":
        case "R":
        case "Stylus":
        case "Zig":
            return `devicon-${language.toLowerCase()}-original`;
        // Indirect mappings
        case "ApacheConf":
            return "devicon-apache-plain";
        case "AppleScript":
            return "devicon-apple-original";
        case "C#":
            return "devicon-csharp-plain";
        case "C++":
            return "devicon-cplusplus-plain";
        case "CSS":
            return "devicon-css3-plain";
        case "Dockerfile":
            return "devicon-docker-plain";
        case "EmberScript":
            return "devicon-ember-original-wordmark";
        case "F#":
            return "devicon-fsharp-plain";
        case "HTML":
            return "devicon-html5-plain";
        case "JetBrains MPS":
            return "devicon-jetbrains-plain";
        case "Jupyter Notebook":
            return "devicon-jupyter-plain";
        case "Less":
            return "devicon-less-plain-wordmark";
        case "Nix":
            return "devicon-nixos-plain";
        case "Objective-C":
            return "devicon-objectivec-plain";
        case "PLSQL":
            return "devicon-oracle-original";
        case "PLpgSQL":
            return "devicon-postgresql-plain";
        case "Unity3D Asset":
            return "devicon-unity-original";
        case "UnrealScript":
            return "devicon-unrealengine-original";
        case "Vue":
            return "devicon-vuejs-plain";
        // Multi-mappings
        case "Go":
        case "Go Checksums":
        case "Go Module":
            return "devicon-go-plain";
        case "Perl":
        case "Perl 6":
            return "devicon-perl-plain"
        case "QMake":
        case "Qt Script":
            return "devicon-qt-original";
        case "Shell":
        case "ShellSession":
            return "devicon-bash-plain";
        case "Vim Script":
        case "Vim Snippet":
            return "devicon-vim-plain";
        case "Microsoft Visual Studio Solution":
        case "Visual Basic":
        case "Visual Basic .NET":
        case "Visual Basic 6.0":
            return "devicon-visualstudio-plain";
        // No mapping
        case "Smalltalk":
        default:
            return "";
    }
});

Handlebars.registerHelper("octicon", function (key) {
    switch (key) {
        case "commits":
            return "commit";
        case "watchers":
            return "eye";
        case "stars":
            return "star";
        case "forks":
            return "repo-forked";
        case "branches":
            return "git-branch";
        case "contributors":
            return "people";
        case "totalIssues":
        case "openIssues":
            return "issue-opened";
        case "totalPullReqs":
        case "openPullReqs":
            return "git-pull-request";
        case "releases":
            return "tag";
        case "size":
            return "file-code";
        case "created":
            return "plus"
        case "updated":
            return "pencil";
        case "lastPush":
            return "arrow-up";
        case "lastCommit":
            return "file-diff";
        case "codeLines":
            return "code-square";
        case "commentLines":
            return "comment";
        case "lines":
            return "project-roadmap";
        default:
            return "question";
    }
});

Handlebars.registerHelper("other-styles", function (key) {
   switch (key) {
       case "openIssues":
       case "openPullReqs":
           return "text-danger";
       default: return "";
   }
});
