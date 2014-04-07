/****************************************** TODO *************************************
2. make the vm list table mulit-selectable. ( use CTRL for multi-select ).
3. add identification tags to each listed vm.
4. add more columns to the listing table.
5. add tooltips where necessary.
6. get host name from backend.
7. start off with dynamic listing.
9. units to be added for props.
**************************************************************************************/

/***************************************** TODO **************************************
1. list out more parameters to add to host summary
    *. no. of vms two columns [ active vms, inactive vms ]
    *. no. of pools
    *. no. of networks [optional]
3. change the layout of the summary tab to a 3 columned table with alternate header and body.
4. add the percentage usage to entries in storage list.
4. add the capacity, allocation into tooltip.
****************************************************************************************/

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

$("#summary .fa-refresh").on("click", function( event ) {
    removeSummary();
    hostName.forEach( getHostInfo );
});

$("#static-list .fa-refresh").on("click", function( event ) {
    removeStaticList();
    hostName.forEach( getStaticList );
});

$("#static-list .fa-play").on("click", function( event ) {
    var vmName = $(".vm-row-selected td:nth-child(2)").html();
    $.ajax({
        url: "/vm/start",
        type: "PUT",
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
    var vmName = $(".vm-row-selected td:nth-child(2)").html();
    console.log(vmName);
    $.ajax({
        url: "/vm/delete",
        type: "DELETE",
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
    var vmName = $(".vm-row-selected td:nth-child(2)").html();
    console.log(vmName);
    $.ajax({
        url: "/vm/shutdown",
        type: "PUT",
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
    var vmName = $(".vm-row-selected td:nth-child(2)").html();
    console.log(vmName);
    $.ajax({
        url: "/vm/poweroff",
        type: "PUT",
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

$("#storage-bar .fa-refresh").on("click", function (event) {
    getStorageList(hostName[0]);
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
    $.getJSON( "/host/info?hostName=" + elem, function( resp ) {
        var propList = [];
        
        for ( var prop in resp ) {
            if( prop != undefined ) propList.push(prop);
        }
        
        $("#summary table tr").each( function () {
            var temp = propList.shift();
            var temp2 = 0;
            var unit = "";
            if( temp == 'memory'){
                resp[temp] = (Number.toInteger(parseInt(resp[temp]) / 1024)).toString();
                unit = "<span class=\"unit\"> MB </span>";
            }
            else if ( temp == 'mhz' ) {
                unit = "<span class=\"unit\"> MHz </span>";
            }
            console.log(typeof temp + temp);
            
            $( this ).append("<td class=\"values\">" + resp[temp] + unit + "</td>");
        });
    });
};

function getStaticList( elem ) {
    $.getJSON("/vm/list/configuration?hostName=" + elem + "&filter=2", function( resp ) {
        var propList = [], str = "";
        var temp;
        
        for( var prop in resp[0] ) {
            if( prop != undefined ) propList.push(prop);
        }
        
        for( var i = 0; i < resp.length ; i++ ){
            str += "<tr>";
            str += "<td>" + (i+1) + "</td>";
            for( var j = 0; j < propList.length ; j++ ) {
                if ( j === 3 ){
                    resp[i][propList[j]] /= 1024;
                    str += "<td>" + resp[i][propList[j]] + "<span class=\"unit\"> MB <span>" + "</td>";
                }
                else {
                    str += "<td>" + resp[i][propList[j]] + "</td>";
                }
            }
            str += "</tr>";
        }
        
        $("#static-list table tbody").append(str);
    });
};

function getStorageList (elem) {
    console.log(elem);
    $(".storage-list > li").remove();
    $.ajax({
        url: "/storage/pool/list",
        type: "GET",
        data: { "hostName": elem, "filter": "2" },
        dataType: "json",
        success: function (pools) {
            for (var i = 0; i < pools.length; i++) {
                var str = "<li id=\"pool" + i + "\"><i class=\"fa fa-caret-right fa-caret-down\"></i><span>" + pools[i].Name + "</span><ol style=\"display: none\"></ol></li>";
                $("#storage-bar > ol.storage-list").append(str);  
                $.ajax({
                    url: "/storage/vol/list",
                    type: "GET",
                    data: { "hostName": elem, "poolName": pools[i].Name, "filter": "2" },
                    dataType: "json",
                    context: $('#pool' + i),
                    success: function (vols) {
                        var str = "";
                        var circle_id = "";
                        var temp_number = 0;
                        
                        console.log(vols);
                        for (var j = 0; j < vols.length; j++) {
                            circle_id = $(this).attr('id') + j;
                            console.log(circle_id);
                            str = "<li>" + vols[j].Name + "<div id=\"" + circle_id +"\" class=\"circles\"></div>" + "</li>";
                            $(this).find('ol').append(str);
                            temp_number = vols[j].Allocation;
                            Circles.create({
                                id: circle_id,
                                percentage: temp_number,
                                radius: 20,
                                width: 6,
                                number: temp_number,
                                text: '',
                                colors: ['#aaa', '#aaf'],
                                duration: null
                            });
                        }
                    },
                    error: function (xhr, resp) {
                        alert("error while getting volume list");
                    }
                });
            }
        },
        error: function (xhr, resp) {
            alert("error while getting pool list");
        }
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
    
    /*********************Create vm ****************************/
    $('#vm-form').on("submit",function () {
    var VMParam = {
	name: document.getElementById("vm-name ").textContent,
	vcpu:document.getElementById("vcpu").value,
	os: document.getElementById("os").value,
	bootdev: document.getElementById("bootdev ").value,
	memory: document.getElementById("ram ").value
};
    console.log(VMParam);
	$.ajax({
		url: '/vm/create',
		type: 'POST',
		contentType: 'application/json',
		//datatype: 'json',
		data: JSON.stringify(VMParam),
		success: function (data, textStatus, jqXHR) {
			alert("VM Created Succesfully !! ");
			
		},
		error: function (xhr, status) {
			alert("	Sorry VM can not be created!");
			
		},
	})
});


/****************** storage listing ******************************/

$("#storage-bar").on("click", ".storage-list > li", function (event) {
    $(this).find("ol").slideToggle();
    $(this).find("i").toggleClass("fa-caret-right");
});

/****************** circles **************************************/
/******************** tool tips *****************************/
/*var pool_id_str = "";
for (var iter = 0; iter < 2; iter++) {
    pool_id_str = "#pool" + (iter+1);
    $(pool_id_str).qtip({
        content: {
            text: pool_list[iter].name
        }
    }); 
}
/*
$('#pool1').qtip({
    content: 'pool1'
});*/