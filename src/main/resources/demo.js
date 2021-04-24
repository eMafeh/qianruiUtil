document.getElementsByClassName("layui-container")[0].style.display = "";
let aaa = (a, b) => document.getElementsByName(a)[0].value = b;
aaa("school", 24);
aaa("student", "王昊阳");
aaa("sex", "男");
aaa("birthday", "2017-08-19");
aaa("id_card", "130425201708193848");
aaa("address", "河北省邯郸市大名县广元小区南院十二号楼一单元303");
aaa("parent", "陈然然");
aaa("tell", "18730031700");
aaa("time", "1596243601");
let children = document.getElementById("").children;
for (const child of children) {
    console.log(child);
}

pagination = new Ext.lt.dc.fill.formulasedittable.pagination(config, service, this);
const div = document.createElement("div");
div.id = "dcTablePaginationDiv";
div.classList.add("layui-table-page");
el.appendChild(div);
pagination.draw();
table.bottomElement = div;

分页栏
position: absolute;
bottom: 0
px;

_gather


_layout
datatable3
.0.js

_resize
table.resize
or
table.reflash

table.resize
bus_datatable.js
pdm.js