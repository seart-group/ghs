(function ($, crypto, Handlebars, Toast, Alert) {
    const $toast_template = $("#template-toast").html();
    const $toast_content = Handlebars.compile($toast_template);
    const $alert_template = $("#template-alert").html();
    const $alert_content = Handlebars.compile($alert_template);

    // https://stackoverflow.com/a/2117523/17173324
    const UUIDV4 = function () {
        return ([1e7] + -1e3 + -4e3 + -8e3 + -1e11).replace(/[018]/g, (c) =>
            (c ^ (crypto.getRandomValues(new Uint8Array(1))[0] & (15 >> (c / 4)))).toString(16),
        );
    };

    $.fn.twbsToast = function (options) {
        const context = $.extend(
            {
                id: `toast-${UUIDV4()}`,
                body: "Lorem Ipsum",
            },
            options,
        );
        const html = $toast_content(context);
        this.append(html);
        const $element = this.find(`#${context.id}`);
        const [element] = $element.get();
        new Toast(element).show();
        $element.on("hidden.bs.toast", () => $element.remove());
        return this;
    };

    $.fn.twbsAlert = function (options) {
        const context = $.extend(
            {
                id: `alert-${UUIDV4()}`,
                body: "Lorem Ipsum",
            },
            options,
        );
        const html = $alert_content(context);
        this.append(html);
        const $element = this.find(`#${context.id}`);
        const [element] = $element.get();
        new Alert(element);
        $element.on("closed.bs.alert", () => $element.remove());
        return this;
    };
})(jQuery, crypto, Handlebars, bootstrap.Toast, bootstrap.Alert);
