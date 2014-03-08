/****************************************** TODO *************************************
1. suppress multiple selection while on summary tab.
2. make the vm list table mulit-selectable. ( use CTRL for multi-select ).
3. add identification tags to each listed vm.
4. add more columns to the listing table.
5. add tooltips where necessary.
6. get host name from backend.
7. start off with dynamic listing.
8. add event handler for sidebar generation.
9. units to be added for props.
**************************************************************************************/

var hostName = [];
var newTabFunctions = {
    'tab-1' : /* function */
        function() {
            hostName.forEach( getHostInfo );
        },
    'tab-2' : /* function */
        function() {
            hostName.forEach( getStaticList );
        },
    'tab-3' : /* function */
        function() {
            console.log("new tab-3");
        },
    'tab-4' : /* function */
        function() {
            console.log("new tab-4");
    }
};

var oldTabFunctions = {
    'tab-1' : /* function */
        function() {
            removeSummary();
        },
    'tab-2' : /* function */
        function() {
            removeStaticList();
        },
    'tab-3' : /* function */
        function() {
            console.log("old tab-3");
        },
    'tab-4' : /* function */
        function() {
            console.log("old tab-4");
        }
};

/************************************* button event handlers ******************************/

$(".icons").click( function ( event ) {
        $(".icons-select").removeClass("icons-select");
        $( this ).addClass("icons-select");
});

/************************* sidebar refresh event handler ******************/
$("#sidebar .fa-refresh").on("click", function( event ) {
        $.getJSON("/host/list", function( json ) {
            $("#selectable li").remove();
            var i = 0;
            var str = "";
            while (json[i++]) {
                str += "<li class=\"ui-widget-content\">" + json[i - 1] + "</li>";
            }
            $("#selectable").append(str);
        });
});

/************************* summary refresh event handler *********************/
$("#summary .fa-refresh").on("click", function( event ) {
    removeSummary();
    hostName.forEach( getHostInfo );
});

/************************** static list refresh event handler ****************/

$("#static-list .fa-refresh").on("click", function( event ) {
    removeStaticList();
    hostName.forEach( getStaticList );
});

$("#static-list .fa-play").on("click", function( event ) {
    var vmName = $(".vm-row-selected td:nth-child(3)").html();
    $.ajax({
        url: "/vm/start",
        type: "GET",
        data: { "vmName": vmName, "hostName" : hostName[0] },
        dataType: 'text',
        success: function (resp) {
            console.log( resp );
            $("#static-list .fa-refresh").triggerHandler("click");
        },
        error: function (xhr, status) {
            alert("sorry there was a problem!");
        }
    });
});

$("#static-list .fa-times").on("click", function( event ) {
    var vmName = $(".vm-row-selected td:nth-child(3)").html();
    console.log(vmName);
    $.ajax({
        url: "/vm/delete",
        type: "GET",
        data: { "vmName" : vmName, "hostName" : hostName[0] },
        dataType: "text",
        success: function( resp ) {
            console.log(resp);
            $("#static-list .fa-refresh").triggerHandler("click");
        },
        error: function (xhr, status) {
            alert("sorry there was a problem");
        }
    });
    $("#inner-content .fa-refresh").trigger("click");
});

$("#static-list .fa-stop").on("click", function( event ) {
    var vmName = $(".vm-row-selected td:nth-child(3)").html();
    console.log(vmName);
    $.ajax({
        url: "/vm/shutdown",
        type: "GET",
        data: { "vmName" : vmName, "hostName" : hostName[0] },
        dataType: "text",
        success: function( resp ) {
            console.log(resp);
            $("#static-list .fa-refresh").triggerHandler("click");
        },
        error: function (xhr, status) {
            alert("sorry there was a problem");
        }
    });
});

$("#static-list .fa-power-off").on("click", function( event ) {
    var vmName = $(".vm-row-selected td:nth-child(3)").html();
    console.log(vmName);
    $.ajax({
        url: "/vm/destroy",
        type: "GET",
        data: { "vmName" : vmName, "hostName" : hostName[0] },
        dataType: "text",
        success: function( resp ) {
            console.log(resp);
            $("#static-list .fa-refresh").triggerHandler("click");
        },
        error: function (xhr, status) {
            alert("sorry there was a problem");
        }
    });
});

/****************************************** remove functions ***********************************/

function removeSummary() {
    $("#summary table tr > td.values").remove();   
};

function removeStaticList() {
    $("#static-list table tbody tr").remove();
};

/******************************************** hostName functions *********************************/
function getHostInfo( elem ) {
    $.getJSON( "/host/info/" + elem, function( resp ) {
        var propList = [];
        
        for ( var prop in resp ) {
            if( prop != undefined ) propList.push(prop);
        }
        
        $("#summary table tr").each( function () {
            $( this ).append("<td class=\"values\">" + resp[propList.shift()] + "</td>");
        });
    });
};

function getStaticList( elem ) {
    $.getJSON("/vm/list/configuration/" + elem + "?filter=2", function( resp ) {
        var propList = [], str = "";
        
        for( var prop in resp[0] ) {
            if( prop != undefined ) propList.push(prop);
        }
        
        for( var i = 0; i < resp.length ; i++ ){
            str += "<tr>";
            str += "<td>" + i + "</td>";
            for( var j = 0; j < propList.length ; j++ ) {
                    str += "<td>" + resp[i][propList[j]] + "</td>";
            }
            str += "</tr>";
        }
        
        $("#static-list table tbody").append(str);
    });
};

/********************************* selectable event handlers *******************************/

$("#selectable").selectable({
    selected: function( event, ui ) {
        var str = $(ui.selected).text();
        
        if( hostName.indexOf(str) == -1 )
            hostName.push(str);
    },
    
    unselected: function( event, ui ) {
        var str = $(ui.unselected).text();
        
        if( hostName.indexOf(str) != -1 )
            hostName.pop(str);
    }
});

/*************************** Add the tabbing event handlers here********************************/
$("#inner-content").tabs({
    active: 0,
    activate: function( event, ui ) {
        var newTab = $(ui.newTab).attr("id"), oldTab = $(ui.oldTab).attr("id");
        $(ui.newTab).addClass("tab-select");
        $(ui.oldTab).removeClass("tab-select");
        
        if( newTabFunctions[newTab] )
            newTabFunctions[newTab]();
        
        if( oldTabFunctions[oldTab] )
            oldTabFunctions[oldTab]();
    },
});

/***************************** get sidebar host list **********************************************/
$.ajax({
    url: "/host/list",
    type: "GET",
    dataType: 'json',
    success: function (json) {
        var i = 0;
        var str = "";

        while (json[i++]) {
            str += "<li class=\"ui-widget-content\">" + json[i - 1] + "</li>";
        }

        $("#selectable").append(str);
    },
    error: function (xhr, status) {
        alert("sorry there was a problem!");
    },
});

/**************************** VMs table selection ************************************************/

$("#static-list table.inner-table > tbody").on("click", "tr", function( event ) {
    console.log("row selected");
    $(".vm-row-selected").removeClass("vm-row-selected");
    $( this ).addClass("vm-row-selected");
});

/***************************** ajax event handlers *********************/

$(document).ajaxStart( function( event ) {
    console.log("ajax query start");
});

$(document).ajaxStop( function( event ) {
    console.log("ajax query ends");
});

/******** note **********
1. ajaxStart is triggered when an ajax query starts and it is not triggered for all until all the pending queries
have been completed

2. ajaxStop is triggered when no other ajax queries are pending

3. ajaxSend is triggered when any ajax quey is about to be sent

4. ajaxComplete is triggered when any ajax query reaches completion

Google like footnotes to be implemented using this.
**********************/

/******************* monitoring **************************/
$(function () {
        $('#container').highcharts({
            title: {
                text: 'Monthly Average Temperature',
                x: -20 //center
            },
            subtitle: {
                text: 'Source: WorldClimate.com',
                x: -20
            },
            xAxis: {
                categories: ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun',
                    'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec']
            },
            yAxis: {
                title: {
                    text: 'Temperature (°C)'
                },
                plotLines: [{
                    value: 0,
                    width: 1,
                    color: '#808080'
                }]
            },
            tooltip: {
                valueSuffix: '°C'
            },
           series: [{
                name: 'Tokyo',
                data: [7.0, 6.9, 9.5, 14.5, 18.2, 21.5, 25.2, 26.5, 23.3, 18.3, 13.9, 9.6]
            }]
        });
    });
    