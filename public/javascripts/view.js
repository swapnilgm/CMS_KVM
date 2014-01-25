/****************************************** TODO *************************************
1. suppress multiple selection while on summary tab.
2. make the vm list table mulit-selectable. ( use CTRL for multi-select ).
3. add identification tags to each listed vm.
4. add status to the vm listing. ( add more columns to the listing table ).
5. add tooltips where necessary.
6. get host name from backend.
7. start off with dynamic listing.
**************************************************************************************/

var hostName = [];

/****************************************** button event handlers ******************************/

$(".icons").click( function ( event ) {
        $(".icons-select").removeClass("icons-select");
        $( this ).addClass("icons-select");
});

$(".fa-refresh").on("click", function( event ) {
    var tab = $("#inner-content").tabs("option", "active");
    
    switch ( tab ){
            case 0 : //summary refresh
                removeSummary();
                hostName.forEach( getHostInfo );
                break;
            case 1 : //static listing refresh
                removeStaticList();
                hostName.forEach( getStaticList );
                break;
            case 3 : //network refresh
                break;
            case 4 : //monitoring refresh
    }
    
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
    active: 0,
    activate: function( event, ui ) {
        var newStr = $(ui.newTab).children("a").text(), oldStr = $(ui.oldTab).children("a").text();
        /*************** the text based method used above is buggy find a better one **********************/
        $(ui.newTab).addClass("tab-select");
        $(ui.oldTab).removeClass("tab-select");

        if(newStr == "Summary") hostName.forEach( getHostInfo );
        if(oldStr == "Summary") removeSummary();
        if(newStr == "Static Listing") hostName.forEach( getStaticList );
        if(oldStr == "Static Listing") removeStaticList();
    },
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
