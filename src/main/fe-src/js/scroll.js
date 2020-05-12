$document.ready(function(){
    $window.scroll(function () {
        if ($(this).scrollTop() > 500) {
            $back_to_top.fadeIn();
        } else {
            $back_to_top.fadeOut();
        }
    });

    $back_to_top.click(function () {
        $body_html.animate({ scrollTop: 0 }, 400);
        return false;
    });

    $first_button.click(function () {
        $body_html.animate({ scrollTop: 0 }, 400);
        return false;
    });

    $prev_button.click(function () {
        $body_html.animate({ scrollTop: 0 }, 400);
        return false;
    });

    $next_button.click(function () {
        $body_html.animate({ scrollTop: 0 }, 400);
        return false;
    });

    $last_button.click(function () {
        $body_html.animate({ scrollTop: 0 }, 400);
        return false;
    });
});

$modal.on('show.bs.modal', function () {
    $modal_body.animate({ scrollTop: 0 }, 'fast');
});