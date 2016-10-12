var selectedElm = null;
var numclasses = 0;
var dgClasses = {"classes":[], "associations":[]};
var paper = Raphael("canvas", 150000,150000);

Raphael.fn.connection = function (obj1, obj2, line, bg) {
	if(obj1 != null ) {
	    if (obj1.line && obj1.from && obj1.to) {
	        line = obj1;
	        obj1 = line.from;
	        obj2 = line.to;
	    }
	    var bb1 = obj1.getBBox(),
	        bb2 = obj2.getBBox(),
	        p = [{x: bb1.x + bb1.width / 2, y: bb1.y - 1},
	        {x: bb1.x + bb1.width / 2, y: bb1.y + bb1.height + 1},
	        {x: bb1.x - 1, y: bb1.y + bb1.height / 2},
	        {x: bb1.x + bb1.width + 1, y: bb1.y + bb1.height / 2},
	        {x: bb2.x + bb2.width / 2, y: bb2.y - 1},
	        {x: bb2.x + bb2.width / 2, y: bb2.y + bb2.height + 1},
	        {x: bb2.x - 1, y: bb2.y + bb2.height / 2},
	        {x: bb2.x + bb2.width + 1, y: bb2.y + bb2.height / 2}],
	        d = {}, dis = [];
	    for (var i = 0; i < 4; i++) {
	        for (var j = 4; j < 8; j++) {
	            var dx = Math.abs(p[i].x - p[j].x),
	                dy = Math.abs(p[i].y - p[j].y);
	            if ((i == j - 4) || (((i != 3 && j != 6) || p[i].x < p[j].x) && ((i != 2 && j != 7) || p[i].x > p[j].x) && ((i != 0 && j != 5) || p[i].y > p[j].y) && ((i != 1 && j != 4) || p[i].y < p[j].y))) {
	                dis.push(dx + dy);
	                d[dis[dis.length - 1]] = [i, j];
	            }
	        }
	    }
	    if (dis.length == 0) {
	        var res = [0, 4];
	    } else {
	        res = d[Math.min.apply(Math, dis)];
	    }
	    var x1 = p[res[0]].x,
	        y1 = p[res[0]].y,
	        x4 = p[res[1]].x,
	        y4 = p[res[1]].y;
	    dx = Math.max(Math.abs(x1 - x4) / 2, 10);
	    dy = Math.max(Math.abs(y1 - y4) / 2, 10);
	    var x2 = [x1, x1, x1 - dx, x1 + dx][res[0]].toFixed(3),
	        y2 = [y1 - dy, y1 + dy, y1, y1][res[0]].toFixed(3),
	        x3 = [0, 0, 0, 0, x4, x4, x4 - dx, x4 + dx][res[1]].toFixed(3),
	        y3 = [0, 0, 0, 0, y1 + dy, y1 - dy, y4, y4][res[1]].toFixed(3);
	    var path = ["M", x1.toFixed(3), y1.toFixed(3), "C", x2, y2, x3, y3, x4.toFixed(3), y4.toFixed(3)].join(",");
	    if (line && line.line) {
	        line.bg && line.bg.attr({path: path});
	        line.line.attr({path: path});
	        line.line.attr('arrow-end', 'open-wide-long');
	    } else {
	        var color = typeof line == "string" ? line : "#000";
	        return {
	            bg: bg && bg.split && this.path(path).attr({stroke: bg.split("|")[0], fill: "none", "stroke-width": bg.split("|")[1] || 3}),
	            line: this.path(path).attr({cursor: "move", stroke: color, fill: "none"}).attr('arrow-end', 'open-wide-long'),
	            from: obj1,
	            to: obj2
	        };
	    }
	}
};
Raphael.st.draggable = function() {
  	var me = this,
	lx = 0,
	ly = 0,
	ox = 0,
	oy = 0,
	moveFnc = function(dx, dy) {
		lx = dx + ox;
		ly = dy + oy;
		me.transform('t' + lx + ',' + ly);
		for (var i = dgClasses.associations.length; i--;) {
	        paper.connection(dgClasses.associations[i].shapeConnection);
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
function selectElement() {
	if(selectedElm==null) {
		selectedElm = this;
		dg_type= selectedElm.data("dg_type");
		$(".prop").hide();
		$(".prop."+dg_type).show();
		// class name or attributte
		if(dg_type == "attribute" || dg_type == "class_name") {
			this.attr({"font-weight":"bold"});
			$(".prop input[name=name]").val(selectedElm.attrs.text);
			$("#properties input[name=key]").val(selectedElm.data("class_key"));
			$("#attrId").val(selectedElm.data("attr_id"));
		} // assocation
		else if (dg_type == "association") {
			this.attr({"stroke-width":2});
			$("#properties input[name=key]").val(selectedElm.data("association_key"));
		} 
		
		// class name or attributte
		if(selectedElm.data("dg_type")=="attribute" ) {
			fillAttributeFields(selectedElm.data("class_key"), selectedElm.data("attr_id"));
			$("select[name=type]").change();
		} // assocation
		else if(selectedElm.data("dg_type")=="association") {
			fillAssociationFields(selectedElm.data("association_key"));
		}
		
		$(".validator").change();
		
	} else {
		$(".prop").hide();
		selectedElm.attr({"font-weight":"normal"});
		selectedElm.attr({"stroke-width":1});
		selectedElm = null;
		$(".prop input[name=name]").val("");
		$("#properties input[name=key]").val("");
		$("#attrId").val("");
		clearFields();
	}
}
fillAssociationFields = function(assoc_key) {
	selectedAssoc = null;
	associations = dgClasses.associations;
	for(cl in associations)	 {
		if(associations[cl].key == assoc_key) {
			selectedAssoc = associations[cl];
			break;
		}
	}
	if(selectedAssoc!=null) {
		$(".prop input[name=name]").val(selectedAssoc.name);
		$(".prop .prop-default_value").val(selectedAssoc.default_value);
		if(selectedAssoc.key_filed){
			$(".prop .prop-key_filed").prop("checked",true);
		}else{
			$(".prop .prop-key_filed").prop("checked", false);
		}
		if(selectedAssoc.mandatory){
			$(".prop .prop-mandatory").prop("checked",true);
		}else{
			$(".prop .prop-mandatory").prop("checked", false);
		}
		if(selectedAssoc.reference){
			$(".prop .prop-reference").prop("checked",true);
		}else{
			$(".prop .prop-reference").prop("checked", false);
		}
		if(selectedAssoc.multiple){
			$(".prop .prop-multiple").prop("checked",true);
		}else{
			$(".prop .prop-multiple").prop("checked", false);
		}
	}
}
fillAttributeFields = function(class_key, attrId) {
	// class_key = data("class_key");
	// attrId = data("attr_id");
	selectedAttr = null;
	seclasses = dgClasses.classes;
	selectedClass = null;
	for(cl in seclasses)	 {
		if(seclasses[cl].key == class_key) {
			//console.log(seclasses[cl]);
			selectedClass = seclasses[cl];

			break;
		}
	}
	if(selectedClass != null) {
		selectedAttr = selectedClass.attributes[attrId];
		$(".prop input[name=name]").val(selectedAttr.name);
		$(".prop select[name=type]").val(selectedAttr.type);
		if(selectedAttr.key_field){
			$(".prop input[name=key_field]").prop("checked",true);
		}else{
			$(".prop input[name=key_field]").prop("checked", false);
		}
		if(selectedAttr.auto_value){
			$(".prop input[name=auto_value]").prop("checked",true);
		}else{
			$(".prop input[name=auto_value]").prop("checked", false);
		}
		if(selectedAttr.key_reference){
			$(".prop input[name=key_reference]").prop("checked",true);
		}else{
			$(".prop input[name=key_reference]").prop("checked", false);
		}
		if(selectedAttr.mandatory){
			$(".prop input[name=mandatory]").prop("checked",true);
		}else{
			$(".prop input[name=mandatory]").prop("checked", false);
		}
		if(selectedAttr.is_calculated){
			$(".prop input[name=is_calculated]").prop("checked",true);
			$(".prop textarea[name=formula]").val(selectedAttr.formula);
		}else{
			$(".prop input[name=is_calculated]").prop("checked", false);
			$(".prop textarea[name=formula]").val("");
		}
		
		if(selectedAttr.requires_validation){
			$(".prop input[name=requires_validation]").prop("checked",true);
			$(".prop textarea[name=validation_formula]").val(selectedAttr.validation_formula);
		}else{
			$(".prop input[name=requires_validation]").prop("checked", false);
			$(".prop textarea[name=validation_formula]").val("");
		}
	
		if(selectedAttr.unicity){
			$(".prop input[name=unicity]").prop("checked",true);
		}else{
			$(".prop input[name=unicity]").prop("checked", false);
		}
		if(selectedAttr.conditional_layout){
			$(".prop input[name=conditional_layout]").prop("checked",true);
			$(".prop textarea[name=conditional_layout_formula]").val(selectedAttr.conditional_layout_formula);
		}else{
			$(".prop input[name=conditional_layout]").prop("checked", false);
			$(".prop textarea[name=conditional_layout_formula]").val("");
		}
		$(".prop input[name=lock_label]").val(selectedAttr.lock_label);
		$(".prop input[name=unlock_label]").val(selectedAttr.unlock_label);
		$(".prop input[name=default_value]").val(selectedAttr.default_value);
		$(".prop input[name=file_title]").val(selectedAttr.file_title);
		$(".prop input[name=file_extension]").val(selectedAttr.file_extension);
		
	}
	$(".prop input[name=name]").val();
}
/**
 * clears property fields
 * @return {[type]} [description]
 */
clearFields = function() {
	$(".prop input[name=name]").val("");
	$(".prop select[name=type]").val("");
	$(".prop input[name=key_field]").prop("checked", false);
	$(".prop input[name=auto_value]").prop("checked", false);
	$(".prop input[name=key_reference]").prop("checked", false);
	$(".prop input[name=mandatory]").prop("checked", false);
	$(".prop input[name=is_calculated]").prop("checked", false);
	$(".prop textarea[name=formula]").val("");
	$(".prop input[name=requires_validation]").prop("checked", false);
	$(".prop textarea[name=validation_formula]").val("");
	$(".prop input[name=assoc_type]").prop("checked", false);
	// $(".prop input[name=assoc_type]").prop("checked", false);
	$(".prop input[name=lock_label]").val("");
	$(".prop input[name=unlock_label]").val("");
	$(".prop input[name=default_value]").val("");
	$(".prop input[name=file_title]").val("");
	$(".prop input[name=file_extension]").val("");
	$(".prop textarea[name=conditional_layout_formula]").val("");
	$(".prop input[name=conditional_layout]").prop("checked", false);
}

/**
 * generates random string
 * @param  {[type]} n [description]
 * @return {[type]}   [description]
 */
function makeid(n){
    var text = "";
    var possible = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";

    for( var i=0; i < n; i++ )
        text += possible.charAt(Math.floor(Math.random() * possible.length));

    return text;
}

function parseBoolean(str) {
	if(str==null)return false;
	if( str==="true") {
		return true ;	
	} 
	if(str===true) {
		return true ;
	}  
	return false;
}
function emptyIfNull(str) {
	if(str == null || str == "") return "\"\"";
	return '"'+str+'"';
}

function checkStrIfNull(str) {
	if(str == null || str == "") return "";
	return str;
}
dgClassestoString = function(dgClasses) {
	var diagram = "{";
	associations = dgClasses.associations;
	stAssoc = "associations:[";
	for(as in associations)	 {
		stAssoc += shapeAssociationtoString(associations[as]);
		if(parseInt(as)+1!= associations.length) {
			stAssoc +=",";
		}
	}
	stAssoc += "]";

	classes = dgClasses.classes;
	stcls = "classes:[";
	for(cl in classes)	 {
		stcls += shapeClasstoString(classes[cl]);
		if(parseInt(cl)+1<classes.length){
			stcls +=",";
		}
	}
	stcls += "]";
	diagram += stAssoc+","+stcls+"}";
	return diagram;

};
/**
 * ShapeClass class
 * @param {[type]} paper [description]
 */

function ShapeClass(paper, obj, classname) {
	this.attributes = {};
	if(obj == null) {
		
		this.key = makeid(9);
		this.className = "ClassName"+(dgClasses.classes.length+1);
		this.x = 50;
		this.y = 50;
		this.height = 100;
		this.width = 170;
		this.classRect = paper.path("M "+this.x+" "+ this.y +" l 0 -35 l "+this.width+" 0 l 0 35 l -"+this.width+" 0 l 0 "+this.height+" l "+this.width+" 0 l 0 -"+this.height);
		this.classRect.attr({fill: "#34495e", stroke: "#34495e", "fill-opacity": 0, "stroke-width": 1, cursor: "move"});

		
		this.classNameShape = paper.text(104, 32, "ClassName"+(dgClasses.classes.length+1)).attr("fill","#34495e").attr("text-anchor", "start");
		this.classNameShape.data({"dg_type":"class_name", "class_key":this.key});
		this.classNameShape.click(selectElement);
		this.btnAddAttribute = paper.text(210, 60, "+").attr("fill","#19bb9c").data({"dg_type":"btn-add-attr", "class_key":this.key});
	
		
		this.btnAddAttribute.click(addAttribute);
	
		this.btnAddAttribute.attr({"font-weight": "bold", "font-size": 16, cursor: "pointer"});
		this.shape = paper.set([this.classRect, this.classNameShape, this.btnAddAttribute]);
		this.shape.draggable();
	} else {
		initShapeClass(obj, classname);
	}
	
	
}
initShapeClass = function ShapeClass( obj, className) {
	
	this.key = obj.key;
	this.className = obj.className;
	this.x = obj.x;
	this.y = obj.y;
	this.height = obj.height;
	this.dx = obj.dx;
	this.dy = obj.dy;
	this.width = obj.width==null?170:obj.width;
	this.classRect = paper.path("M "+this.x+" "+ this.y +" l 0 -35 l "+this.width+" 0 l 0 35 l -"+this.width+" 0 l 0 "+this.height+" l "+this.width+" 0 l 0 -"+this.height);
	this.classRect.attr({fill: "#34495e", stroke: "#34495e", "fill-opacity": 0, "stroke-width": 1, cursor: "move"});
	
	this.classNameShape = paper.text(104, 32, this.className).attr("fill","#34495e").attr("text-anchor", "start");
	this.classNameShape.data({"dg_type":"class_name", "class_key":this.key});
	this.classNameShape.click(selectElement);
	
	this.btnAddAttribute = paper.text(210, 60, "+").attr("fill","#19bb9c").data({"dg_type":"btn-add-attr", "class_key":this.key});
	this.btnAddAttribute.click(addAttribute);

	this.btnAddAttribute.attr({"font-weight": "bold", "font-size": 16, cursor: "pointer"});
	this.shape = paper.set([this.classRect, this.classNameShape, this.btnAddAttribute]);
	this.shape.draggable();
	obj.shape = this.shape;
	obj.classRect = this.classRect;
	obj.shape.translate(obj.dx, obj.dy);
}

drawAttribute = function (attrkey,attrPos, title, shape_class) {
	//num_attr = Object.keys(shape_class.attributes).length+1;
	attr_key = attrkey;
	height = shape_class.height;
	var attribute = paper.text(57, 57+attrPos*10, title).attr("fill","#34495e").attr("text-anchor", "start");
	attribute.translate(shape_class.dx, shape_class.dy);
	attribute.data({"dg_type":"attribute", "attr_id": attr_key, "class_key":shape_class.key});
	attribute.click(selectElement);
	shape_class.shape.push(attribute);
}
addAttribute = function() {
	class_key = this.data("class_key");
	classes = dgClasses.classes;
	shape_class = null;
	for(cl in classes)	 {
		if(classes[cl].key == class_key) {
			console.log(classes[cl]);
			// classes[cl].className = $("#properties input[name=name]").val();
			shape_class = classes[cl];

			break;
		}
	}
	if(shape_class != null) {
		var an_attribute={};

		//num_attr = Object.keys(attrs).length+1;
		num_attr = Object.keys(shape_class.attributes).length+1;
		attrKey = "attribute_"+num_attr;
		height = shape_class.height;
		width = shape_class.width;
		var attribute = paper.text(57, 53+num_attr*10, "attribute"+num_attr).attr("fill","#34495e").attr("text-anchor", "start");
		attribute.translate(this._.dx, this._.dy);
		attribute.data({"dg_type":"attribute", "attr_id":attrKey, "class_key":class_key});
		
		this.attr({ y:53+num_attr*10});

		shape_class.attributes[attrKey]= {"name":"attribute"+num_attr, "type":"Number"};
		
		if(num_attr*10>height-20) {
			//console.log(classRect.attrs.path);
			height=height+30;
			shape_class.classRect.attr("path","M 50 50 l 0 -35 l "+width+" 0 l 0 35 l -"+width+" 0 l 0 "+height+" l "+width+" 0 l 0 -"+height );
			shape_class.height = height;
		}

		attribute.click(selectElement);
		shape_class.shape.push(attribute);
	}
}

shapeClasstoString = function(shape_class){
	return '{'
		+'"key":"'+shape_class.key+'",'
		+'"className":"'+shape_class.className+'",'
		+'"x":'+shape_class.x+','
		+'"y":'+shape_class.y+','
		+'"dx":'+shape_class.shape[0]._.dx+','
		+'"dy":'+shape_class.shape[0]._.dy+','
		+'"height":'+shape_class.height+','
		+'"width":'+shape_class.width+','
		+'"attributes":'+attrToString(shape_class.attributes)
	+'}';
}
attrToString = function(attributes) {
	s = '{';
	keys = Object.keys(attributes);
	for( i in keys){
		attr = attributes[keys[i]];
		s+= keys[i]+':{'
			+'"key_field":'+parseBoolean(attr.key_field)+','
			+'"auto_value":'+parseBoolean(attr.auto_value)+','
			+'name:'+emptyIfNull(attr.name)+','
			+'type:'+emptyIfNull(attr.type)+','
			+'key_reference:'+parseBoolean(attr.key_reference)+','
			+'mandatory:'+parseBoolean(attr.mandatory)+','
			+'is_calculated:'+parseBoolean(attr.is_calculated)+','
			+'formula:'+emptyIfNull(attr.formula)+','
			+'requires_validation:'+parseBoolean(attr.requires_validation)+','
			+'validation_formula:'+emptyIfNull(attr.validation_formula)+','
			+'unicity:'+parseBoolean(attr.unicity)+','
			+'conditional_layout:'+parseBoolean(attr.conditional_layout)+','
			+'conditional_layout_formula:'+emptyIfNull(attr.conditional_layout_formula)+','
			+'lock_label:'+emptyIfNull(attr.lock_label)+','
			+'unlock_label:'+emptyIfNull(attr.unlock_label)+','
			+'default_value:'+emptyIfNull(attr.default_value)+','
			+'file_title:'+emptyIfNull(attr.file_title)+','
			+'file_extension:'+emptyIfNull(attr.file_extension)
		+'}';
		if(parseInt(i) < keys.length-1) {
			s += ","
		}
	}
	return s+"}";
		
}
/**
 * ShapeAssociation class
 * @param {[type]} paper         [description]
 * @param {[type]} classA        [description]
 * @param {[type]} classB        [description]
 * @param {[type]} multiplicityA [description]
 * @param {[type]} multiplicityB [description]
 */
function ShapeAssociation(paper, classA, classB, multiplicityA , multiplicityB, obj) {
	if(obj == null) {
		this.classAName = classA.className;
		this.classBName = classB.className;
		this.name=classA.className+"__"+classB.className;
		this.reference = true;
		this.multiple = false;
		this.key = makeid(6);
	
		this.classA = classA;
		this.classB = classB;
		this.multiplicityA = multiplicityA;
		this.multiplicityB = multiplicityB;
//		console.log(this.classA.shape);
		this.shapeConnection = paper.connection(this.classA.shape, this.classB.shape, "#34495e");
		if(this.shapeConnection.line) {
			this.shapeConnection.line.data({"dg_type":"association", "association_key":this.key});
			this.shapeConnection.line.click(selectElement);
		}
	} else {
		obj.classAName = checkStrIfNull(obj.classAName);
		obj.classBName = checkStrIfNull(obj.classAName);
		obj.name=checkStrIfNull(obj.name);
		obj.reference = parseBoolean(obj.reference);
		obj.multiple = parseBoolean(obj.mutiple);
		obj.key = checkStrIfNull(obj.key);
		obj.multiplicityA = obj.multiplicityA==null?1:obj.multiplicityA;
		obj.multiplicityB = obj.multiplicityB==null?1:obj.multiplicityB;
		this.classA = classA;
		this.classB = classB;
		obj.shapeConnection = paper.connection(this.classA.shape, this.classB.shape, "#34495e");
		if(obj.shapeConnection.line) {
			obj.shapeConnection.line.data({"dg_type":"association", "association_key":obj.key});
			obj.shapeConnection.line.click(selectElement);
		}
		obj.classA = classA;
		obj.classB = classB;
	}
	
}

shapeAssociationtoString = function(shapeAssociation){
	return '{'
		+'"key":'+emptyIfNull(shapeAssociation.key)+','
		+'"key_field":'+parseBoolean(shapeAssociation.key_field)+','
		+'"default_value":'+emptyIfNull(shapeAssociation.default_value)+','
		+'"name":'+emptyIfNull(shapeAssociation.name)+','
		+(shapeAssociation.classA!=null?'"classAName":'+emptyIfNull(shapeAssociation.classA.className):shapeAssociation.classAName) +','
		+(shapeAssociation.classB!=null?'"classBName":'+emptyIfNull(shapeAssociation.classB.className):shapeAssociation.classBName) +','
		+'"reference":'+parseBoolean(shapeAssociation.reference)+','
		+'"mandatory":'+parseBoolean(shapeAssociation.mandatory)+','
		+'"multiple":'+parseBoolean(shapeAssociation.multiple)+','
		+'"multiplicityA":'+shapeAssociation.multiplicityA+','
		+'"multiplicityB":'+shapeAssociation.multiplicityB
	+'}';
};

/**
 * modify elements property 
 * @return {[type]} [description]
 */
modifyProperty = function() {
	if(selectedElm != null) {
		if (selectedElm.data("dg_type") == "attribute" || selectedElm.data("dg_type") == "class_name"){
			class_key = $("#properties input[name=key]").val();
			selectedElm.attr("text", $("#properties input[name=name]").val());
			classes = dgClasses.classes;
			slClass = null;
			for(cl in classes)	 {
				if(classes[cl].key == class_key) {
					console.log(classes[cl]);
					// classes[cl].className = $("#properties input[name=name]").val();
					slClass = classes[cl];

					break;
				}
			}
			if(slClass != null) {
				if(selectedElm.data("dg_type") == "class_name") {
					slClass.className = $("#properties input[name=name]").val();
				} else {
					slClass.attributes[$("#attrId").val()]["name"]= $("#properties input[name=name]").val();
					slClass.attributes[$("#attrId").val()]["type"]= $("#properties select[name=type]").val();
					slClass.attributes[$("#attrId").val()]["key_field"]= $("#properties input[name=key_field]").is(":checked");
					slClass.attributes[$("#attrId").val()]["auto_value"]= $("#properties input[name=auto_value]").is(":checked");
					slClass.attributes[$("#attrId").val()]["key_reference"]= $("#properties input[name=key_reference]").is(":checked");
					slClass.attributes[$("#attrId").val()]["mandatory"]= $("#properties input[name=mandatory]").is(":checked");
					slClass.attributes[$("#attrId").val()]["is_calculated"]= $("#properties input[name=is_calculated]").is(":checked");
					slClass.attributes[$("#attrId").val()]["formula"]= $("#properties textarea[name=formula]").val();
					slClass.attributes[$("#attrId").val()]["requires_validation"]= $("#properties input[name=requires_validation]").is(":checked");
					slClass.attributes[$("#attrId").val()]["validation_formula"]= $("#properties textarea[name=validation_formula]").val();
					slClass.attributes[$("#attrId").val()]["file_title"]= $("#properties input[name=file_title]").val();
					slClass.attributes[$("#attrId").val()]["file_extension"]= $("#properties input[name=file_extension]").val();
					slClass.attributes[$("#attrId").val()]["lock_label"]= $("#properties input[name=lock_label]").val();
					slClass.attributes[$("#attrId").val()]["unlock_label"]= $("#properties input[name=unlock_label]").val();
					slClass.attributes[$("#attrId").val()]["default_value"]= $("#properties input[name=default_value]").val();
					slClass.attributes[$("#attrId").val()]["unicity"]= $("#properties input[name=unicity]").is(":checked");
					slClass.attributes[$("#attrId").val()]["conditional_layout"]= $("#properties input[name=conditional_layout]").is(":checked");
					slClass.attributes[$("#attrId").val()]["conditional_layout_formula"]= $("#properties textarea[name=conditional_layout_formula]").val();
				} 
			}
		} else if (selectedElm.data("dg_type") == "association"){
			console.log("association");
			association_key = $("#properties input[name=key]").val();
			associations = dgClasses.associations;
			for(cl in associations)	 {
				if(associations[cl].key == association_key) {
					selectedAssoc = associations[cl];
					break;
				}
			}
			if(selectedAssoc != null) {
				selectedAssoc["name"]= $("#properties input[name=name]").val();
				selectedAssoc["key_field"]= $("#properties .key_field").prop("checked");
				selectedAssoc["multiple"]= $("#properties .prop-multiple").prop("checked");
				selectedAssoc["reference"]= $("#properties .prop-reference").prop("checked");
				selectedAssoc["mandatory"]= $("#properties .prop-mandatory").prop("checked");
				selectedAssoc["default_value"]= $("#properties .prop-default_value").val();
					
			}
			
			
		}
	}
}

drawGraph = function() {
	classes = dgClasses.classes;
	associations = dgClasses.associations;
	for(i in classes) {
		new ShapeClass(classes[i].className, classes[i]);
		keys = Object.keys(classes[i].attributes);
		for( k in keys) {
			attr = classes[i].attributes[keys[k]];
			drawAttribute(keys[k], k, attr['name'], classes[i]);
		}
	}
	
	for(a in associations) {
		var classA =null;
		var classB = null;
		for(c in classes) {
			if(classes[c].className == associations[a].classAName){
				classA = classes[c];
			}
			if(classes[c].className == associations[a].classBName){
				classB = classes[c];
			}
			if(classA!=null && classB!=null){
				new ShapeAssociation(paper, classA, classB, 1, 1, associations[a]);
				break;
			}
		}
		
	}
	
}
/*
getLengthestAttr = function(shape_class) {
	lengthest = "";
	keys = Object.keys(shape_class.attributes);
	slAttr = null;
	for( i in keys) {
		attr = shape_class.attributes[keys[i]];
		if(attr.name.length>lengthest.length) {
			lengthest = attr.name;
			slAttr = attr;
		}
	}
	return slAttr;
}

redrawClass = function(shape_class, anew, old){
	var slAttr = null;
	if(anew != old) {
		slAttr = getLengthestAttr(shape_class);
		if(slAttr!=null && slAttr.name ==  anew && slAttr.name.length > 11) {
			height = shape_class.height;
			width = shape_class.width==null?100:shape_class.width;
			width = slAttr.name.length*7;
			shape_class.classRect.attr("path","M 50 50 l 0 -35 l "+width+" 0 l 0 35 l -"+width+" 0 l 0 "+height+" l "+width+" 0 l 0 -"+height );
			shape_class.width = width;
			x = shape_class.btnAddAttribute.attr("x");
			shape_class.btnAddAttribute.attr({ "x":width+50-6});
			//slAttr.shape.attr("x",86+Math.abs(anew.length-old.length)*2);
		//	slAttr.translate(shape_class.btnAddAttribute._.dx, shape_class.btnAddAttribute._.dy);
		}
	}
}
*/
$(function() {

	$("#dg-association").click(function() {
		if($("#class-assoc").is(":hidden")) {
			
			$("#slct1,#slct2").html("");
			classes = dgClasses.classes;
			for(cl in classes)	 {
				$("#slct1,#slct2").append("<option value='"+cl+"'>"+classes[cl].className+"</option>");
			}
			$("#class-assoc").show();
		} else {
			$("#class-assoc").hide();
		}
	});
	/**
	 * create new class
	 * @return {[type]} [description]
	 */
	$("#dg-class").click(function(){
		cshape = new ShapeClass(paper);
		dgClasses.classes.push(cshape);
	});
	/**
	 * create association 
	 * @return {[type]} [description]
	 */
	$("#set-assoc").click(function() {
		var iA = $("#slct1").val();
		var iB = $("#slct2").val();

		cassoc = new ShapeAssociation(paper, dgClasses.classes[iA], dgClasses.classes[iB],1,1);
		dgClasses.associations.push(cassoc);
	});
	/**
	 * accept enter key
	 * @param  {[type]} e [description]
	 * @return {[type]}   [description]
	 */
	$('.prop input').keypress(function (e) {
	  if (e.which == 13) {
	    modifyProperty();
	  }
	});
	/**
	 * save properties
	 */
	$("#save-prop").click(modifyProperty);

	/**
	 * check required fields
	 * @return {[type]} [description]
	 */
	$(".validator").change(function(){
		to_validate = $(this).data("valid");
		$(to_validate)
		// console.log(to_validate);
		if(!$(this).is(":checked")) {
			$(to_validate).attr("disabled","disabled");
			$(to_validate).val("");
		} else {
			$(to_validate).removeAttr("disabled");
		}
	});
	$(".validator").change();

	$("select[name=type]").change(function(){
		val = $(this).val();
		$(".prop .controlled").attr("disabled", "disabled");
		$(".prop .controlled."+val).removeAttr("disabled");
	});
	$("select[name=type]").change();
	$(".prop").hide();

});

