function localToUTC(date) {
    let localDateMS = date.getTime();
    let localOffsetMS = date.getTimezoneOffset() * 60000;
    return (new Date(localDateMS - localOffsetMS)).toISOString().slice(0,10);
}

const options = {
    autoclose: true,
    endDate: "0d",
    format: 'yyyy/mm/dd',
    orientation: 'bottom right',
    todayHighlight: true,
    showWeekDays: false,
    startDate: '2008/01/01'
};

$btn_created_min.datepicker(options).on('changeDate', function(ev) {
    $btn_created_min.datepicker('hide');
    $created_min.val(localToUTC(ev.date));
});

$btn_created_max.datepicker(options).on('changeDate', function(ev) {
    $btn_created_max.datepicker('hide');
    $created_max.val(localToUTC(ev.date));
});

$btn_committed_min.datepicker(options).on('changeDate', function(ev) {
    $btn_committed_min.datepicker('hide');
    $committed_min.val(localToUTC(ev.date));
});

$btn_committed_max.datepicker(options).on('changeDate', function(ev) {
    $btn_committed_max.datepicker('hide');
    $committed_max.val(localToUTC(ev.date));
});