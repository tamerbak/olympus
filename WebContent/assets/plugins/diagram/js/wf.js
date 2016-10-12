var borderColor = "#1570a6";
var textColor = "#fff";

var dgWF= {"nodes":{}, "transitions":{}};
var wfSelectedElem = null;
function getColorByType(type) {
	color = "#000";
	switch(type) {
		case "request":color="#7278cf";break;
		case "infos":color="#d33f2b";break;
		case "screen":color="#ffb403";break;
		case "answer":color="#019956";break;
		case "choice":color="#bd8440";break;
		default:color="#000";
	}
	return color;
}
Raphael.st.wfdraggable = function() {
  	var me = this,
	lx = 0,
	ly = 0,
	ox = 0,
	oy = 0,
	moveFnc = function(dx, dy) {
		lx = dx + ox;
		ly = dy + oy;
		me.transform('t' + lx + ',' + ly);
		for (key in dgWF.transitions) {
	        paper.connection(dgWF.transitions[key].shape);
	    }
	},
	startFnc = function() {
	},
	endFnc = function() {
	  ox = lx;
	  oy = ly;
	};
  
	this.drag(moveFnc, startFnc, endFnc);
};
function wfSelectElement() {
	if(wfSelectedElem==null) {
		wfSelectedElem = this;
		wfSelectedElem.attr("stroke-width",2);
		var type = wfSelectedElem.data("dg_type");
		var type_id = wfSelectedElem.data("dgtype_id");
		if(type == "node") {
			$(".node_action .create").hide();
			$(".node_action .save").show();
			var node_key = wfSelectedElem.data("key");
			$("#node_key").val(node_key);
			var node = dgWF.nodes[node_key];
			$("#node_title").val(node.title);
			$("#node_message").val(node.message);
			$("select[name=roles]").val(node.role);
			$("select[name=windows]").val(node.window);
		} else if(type == "transition") {
			console.log(type);
			$("#div-slct1,#div-slct2").hide();
			$("#add-transition").hide();
			$("#delete-transition").show();
			$("#update-transition").show();
			var trans_key = wfSelectedElem.data("key");
			$("#trans_key").val(trans_key);
			var trans = dgWF.transitions[trans_key];
			$("#trans_title").val(trans.title);
			$("#trans_auto").prop("checked", trans.auto);
			$("#yes").prop("checked", trans.yes_no == "yes");
			$("#no").prop("checked", trans.yes_no == "no");
			
		}


	} else {
		wfSelectedElem.attr("stroke-width",1);
		wfSelectedElem=null;
		$(".node_action .create").show();
		$(".node_action .save").hide();
		$("#node_key").val("");
		$("#node_title").val("");
		$("#node_message").val("");

		$("#add-transition").show();
		$("#delete-transition").hide();
		$("#update-transition").hide();
		$("#trans_title").val("");
		$("#trans_auto").prop("checked", false);
		$("#div-slct1,#div-slct2").show();
		$("#yes").prop("checked",true);
		$("#no").prop("checked",false);
	}
}

function WFENode(title, message, type, type_id,role, window, obj) {
	if(obj == null) {
		this.x = 50;
		this.y = 50;
		this.dx = 0;
		this.dy = 0;
		this.width = 100;
		this.height = 40;
		this.key = makeid(9);
		this.color = getColorByType(type);
		this.type=type;
		this.type_id=type_id;
		this.title = title==null?"Node "+type:title;
		this.message = message;
		this.role = role;
		this.window = window;
		
		this.nodeRectangle = paper.rect(this.x,this.y,this.width,this.height).attr({fill: this.color, stroke: this.color, "fill-opacity": 0.8, "stroke-width": 1, cursor: "move"});
		this.nodeTitle = paper.text(this.x+20,this.y+20, this.title).attr("fill", textColor).attr("text-anchor","start");
		this.shape = paper.set([this.nodeRectangle, this.nodeTitle]);
		this.shape.wfdraggable();
		this.shape.data({"dg_type": "node", "key":this.key});
		this.shape.click(wfSelectElement);
	} else {
		obj.nodeRectangle = paper.rectangle(obj.x,obj.y,obj.width,obj.height).attr({fill: "#fff", stroke: "#fff", "fill-opacity": 0, "stroke-width": 1, cursor: "move"});
		obj.nodeTitle = paper.text(obj.x+20,obj.y+20, obj.title).attr("fill", textColor).attr("text-anchor","start");
		obj.shape = paper.set([obj.nodeRectangle, obj.nodeTitle]);
		obj.shape.wfdraggable();
	}
}

wfNodeToString = function( node) {
	return '{'
		+'"key":'+emptyIfNull(node.key)+','
		+'"type":'+emptyIfNull(node.type)+','
		+'"type_id":'+emptyIfNull(node.type_id)+','
		+'"title":'+emptyIfNull(node.title)+','
		+'"message":'+emptyIfNull(node.message)+','
		+'"role":'+emptyIfNull(node.role)+','
		+'"window":'+emptyIfNull(node.window)+','
		+'"color":'+emptyIfNull(node.color)+','
		+'"x":'+node.x+','
		+'"y":'+node.y+','
		+'"dx":'+node.shape[0]._.dx+','
		+'"dy":'+node.shape[0]._.dy+','
	+'}';
};

function WFTransition(title, nodeAKey, nodeBKey, auto,yes_no, obj) {
	if(obj == null) {
		this.key = makeid(8);
		this.title = title;
		this.auto  = auto;
		this.nodeA = dgWF.nodes[nodeAKey];
		this.nodeB = dgWF.nodes[nodeBKey];
		this.yes_no = yes_no;
		this.shape = paper.connection(this.nodeA.shape, this.nodeB.shape, borderColor);
		this.shape.line.data({"dg_type": "transition", "key":this.key});
		this.shape.line.click(wfSelectElement);
	} else {

	}
};
wfTransitionToString = function(trans) {
	return '{'
		+'"key":'+emptyIfNull(trans.key)+','
		+'"title":'+emptyIfNull(trans.title)+','
		+'"auto":'+parseBoolean(trans.auto)+','
		+'"yes_no":'+emptyIfNull(trans.yes_no)+','
		+'"nodeA":'+emptyIfNull(trans.nodeA.title)+','
		+'"nodeAKey":'+emptyIfNull(trans.nodeA.key)+','
		+'"nodeB":'+emptyIfNull(trans.nodeB.title)+','
		+'"nodeBKey":'+emptyIfNull(trans.nodeB.key)+','
	+'}';
};

dgWFToString = function(){
	var nodes = dgWF.nodes;
	var str = '{';
	var strNodes ='nodes:{'
	var n=1;
	nodesLen = Object.keys(nodes).length;
	for(k in nodes) {
		strNodes+='"'+k+'":'+wfNodeToString(nodes[k]);
		if((n++)<nodesLen){
			strNodes+=',';
		}
	}
	strNodes+='}';
	var transitions = dgWF.transitions;
	var strTrans = 'transitions:{';
	var t = 1;
	transLen = Object.keys(transitions).length;
	for(kt in transitions) {
		strTrans += '"'+kt+'":'+wfTransitionToString(transitions[kt]);
		if((t++) < transLen) {
			strTrans += ",";
		}
	}
	strTrans += "}";
	
	str += strNodes+","+strTrans+"}";
	return str;
};
checkTransitionAlreadyExsits = function(keyA, keyB){
	var result = false;
	var transitions = dgWF.transitions;
	for (k in transitions) {
		var tr = transitions[k];
		if(tr.nodeA.key == keyA && tr.nodeB.key == keyB) {
			result = true;
			alert("transition already exists");
			break;
		}
	}
	return result;
};
refreshNodeSelects = function(){
	$("#slct1,#slct2").html("");
		var nodes = dgWF.nodes;
		for(cl in nodes) {
			$("#slct1,#slct2").append("<option value='"+cl+"'>"+nodes[cl].title+"</option>");
		}
		//$("#class-assoc").show();
};
deleteTransition = function(key){

	var obj = dgWF.transitions[key];
	obj.shape.line.remove();
	delete dgWF.transitions[key];
};
$(function() {

	/**
	 * create new class
	 * @return {[type]} [description]
	 */
	$(".add-node").click(function() {
		try{
			var title = $("#node_title").val();
			var message = $("#node_message").val();
			var type = $(this).data("type");
			var type_id = $(this).data("type_id");
			var role = $("select[name=roles]").val();
			var window = $("select[name=windows]").val();
			var wf_node = new WFENode(title, message, type, type_id, role, window);

			dgWF.nodes[wf_node.key] = wf_node;
			//$("#slct1,#slct2").append("<option value='"+wf_node.key+"'>"+wf_node.title+"</option>");
			refreshNodeSelects();
			$("#node_title").val("");
			$("#node_message").val("");
		}catch(error) {
			console.log(error);
		}
	});

	$("#add-transition").click(function() {
		var title = $("#trans_title").val();
		var auto = $("#trans_auto").prop("checked");
		var nodeAKey = $("#slct1").val();
		var nodeBKey = $("#slct2").val();
		var yes_no = $("input[name=yes_no]:checked").val();
		if(!checkTransitionAlreadyExsits(nodeAKey, nodeBKey)) {
			trans = new WFTransition(title, nodeAKey, nodeBKey, auto,yes_no);
			dgWF.transitions[trans.key] = trans;
		}
		
	});

	$("#save-node").click(function(){
		var key = $("#node_key").val();
		var title = $("#node_title").val();
		var msg = $("#node_message").val();
		var role = $("select[name=roles]").val();
		var window = $("select[name=windows]").val();
		var obj = dgWF.nodes[key];
		if(obj != null) {
			obj.title = title;
			obj.message = msg;
			obj.role = role;
			obj.window = window;
			obj.nodeTitle.attr("text", title);
			wfSelectElement();
			refreshNodeSelects();
		}

	});
	$("#update-transition").click(function(){
		var key = $("#trans_key").val();
		var title = $("#trans_title").val();
		var auto = $("#trans_auto").prop("checked");
		var yes_no = $("input[name=yes_no]:checked").val();
		var obj = dgWF.transitions[key];
		if(obj != null) {
			obj.title = title;
			obj.auto = auto;
			obj.yes_no=yes_no;
			wfSelectElement();
		}
	});
	$("#delete-transition").click(function(){
		var resp = confirm("Would you really delete this transition");
		if(resp) {
			key = $("#trans_key").val();
			deleteTransition(key);
			wfSelectElement();
		}

	});

	$("#delete-node").click(function(){
		var resp = confirm("If you delete this node all linked transitions would be delete. \n Do you confirm ?");
		if(resp) {
			key = $("#node_key").val();
			var obj = dgWF.nodes[key];
			if(obj != null) {
				transs = dgWF.transitions;
				toDel = [];
				for(k in transs) {
					if(transs[k].nodeA.key == obj.key ||transs[k].nodeB.key == obj.key){
						toDel.push(k);
					}
				}
				for(i in toDel) {
					deleteTransition(toDel[i]);
				}
				obj.shape.remove();
				// obj.nodeTitle.remove();
				// obj.nodeRectangle.remove();
				delete dgWF.nodes[key];
				wfSelectElement();
			}
		}
	});
	
	$("#dg-submit-btn").click(function(){
		try {
			
			var obj = dgWFToString( );
			$('<input />').attr('type', 'hidden')
	        .attr('name', 'diagram')
	        .attr('value',  obj)
	        .appendTo('#form-wf-diagram');
			
			$('<input />').attr('type', 'hidden')
			.attr('style',"display:none")
	        .attr('name', "source")
	        .attr('value', "fromDC")
	        .appendTo('#form-wf-diagram');
			
			$("#form-wf-diagram").submit();
		} catch(err) {
		   	console.log(err);
			return false;
		}
		
		
	});

});

