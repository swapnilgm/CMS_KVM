var hostName = [];

/****************************************** remove functions ***********************************/

function removeSummary() {
    $("#summary table tr > td.values").remove();   
};

function removeStaticList() {
    $("#static-list table tbody tr").remove();
};

/******************************************** hostName functions *********************************/
function getHostInfo( elem ) {
    $.getJSON( "/hostinfo/" + elem, function( resp ) {
        var propList = [];
        
        for ( var prop in resp ) {
            if( prop != undefined ) propList.push(prop);
        }
        
        $("#summary table tr").each( function () {
            $( this ).append("<td class=\"values\">" + resp[propList.shift()] + "</td");
        });
    });
};

function getStaticList( elem ) {
    $.getJSON("/list/static/" + elem + "?filter=2", function( resp ) {
        var propList = [], str = "";
        
        for( var prop in resp[0] ) {
            if( prop != undefined ) propList.push(prop);
        }
        
        for( var i = 0; i < resp.length ; i++ ){
            str += "<tr>";
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
    activate: function( event, ui ) {
        var newStr = $(ui.newTab).text(), oldStr = $(ui.oldTab).text();
        
        if(newStr == "Summary") hostName.forEach( getHostInfo );
        if(oldStr == "Summary") removeSummary();
        if(newStr == "Static Listing") hostName.forEach( getStaticList );
        if(oldStr == "Static Listing") removeStaticList();
    }
});

/***************************** get sidebar host list **********************************************/
$.ajax({
    url: "/list/host",
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
