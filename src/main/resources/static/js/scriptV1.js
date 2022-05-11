document.addEventListener("DOMContentLoaded", start);

let fieldsToSkipFieldName = "fieldsToSkip";
let additionalListsFieldName = "additionalLists";

var sources;
var condsTable;
var beige;
var additionalListsDiv;
var stringifiedCondsDiv;

let boolClassMap = new Map([[false, 'bool-false'], [true, 'bool-true']]);
var condsHtml = '';
var rowIsOpen = false;

function start() {
    if(!localStorage.getItem('lastPatientId')) localStorage['lastPatientId'] = 2012;

    sources = $("#sources");
    sources[0].style.setProperty("--sew", document.scrollingElement.clientWidth / 100 + "px");
    sources.css("width", "calc(100% - var(--sew))");

    condsTable = $("#condsTable>tbody")[0];
    beige = $("#beige");
    additionalListsDiv = $("#add-lists")[0];
    stringifiedCondsDiv = $("#stringified")[0];

    $("button").on("click", submit);

    let patientIdInput = $("input#patientId");
    patientIdInput[0].value = localStorage['lastPatientId'];
    patientIdInput.on("keyup", ev => {
        if(ev.code === 'Enter') {
            localStorage['lastPatientId'] = ev.target.value;
            submit();
            return false;
        }
    });
}

function submit() {
    sources.html("");
    condsTable.innerHTML = "";
    additionalListsDiv.innerHTML = "";
    stringifiedCondsDiv.innerHTML = "";

    $.ajax({
        url: "http://localhost:9998/rest/v1",
        method: "GET",
        async: true,
        cache: false,
        data: serializeForm(),
        dataType: "json",
        success: (response) => {
            console.log("***************************************************************************");
            console.log(response);
            console.log("***************************************************************************");
            drawData(response);
        }
    });
}

function serializeForm() {
    return {
        patientId: $("input#patientId")[0].value,
        isHfRiskFactor: ($("input#isHfRiskFactor")[0].checked || false),
        isPrevTherapyCheck: ($("input#isPrevTherapyCheck")[0].checked || false)
    };
}

function drawData(data) {
    updateSourceDataView(data.sources);
    updateAdditionalListsView(data.sources[additionalListsFieldName] || []);

    stringifiedCondsDiv.innerText = data.dsl.stringified;

    condsHtml = '';
    rowIsOpen = false;
    buildHtmlForDslElement(data.dsl);
    condsTable.innerHTML = condsHtml;

    $("td.dsl-plain").add($("td.dsl-block-td"))
        .mousemove(onLogicBlockHoverHandler)
        .mouseout(() => beige.toggleClass("beige-hidden", true));
}

function updateSourceDataView(data) {
    let fieldsToSkip = data[fieldsToSkipFieldName] || [];
    let sourcesDivHtml = '<ul>';
    for(var p in data) {
        if(fieldsToSkip.indexOf(p) !== -1 || p === fieldsToSkipFieldName) continue;
        sourcesDivHtml += `<li><span class="sources-param-name">${p}</span> = <span class="sources-param-value">${data[p]}</span></li>`
    }
    sourcesDivHtml += '</ul';
    sources.html(sourcesDivHtml);
}

function updateAdditionalListsView(data) {
    additionalListsDiv.innerHTML = "";
    data
        .filter(list => list.length !== 0)
        .forEach(list => {
            var html = `<table class="table-add-lists shadowed"><thead>`;
            html += `<tr>`;
            for(var p in list[0]) {
                html += `<td>${p}</td>`;
            }
            html += `</tr></thead><tbody>`;

            list.forEach(el => {
                html += `<tr class="shadowed">`;
                for(var p in el) {
                    html += `<td>${el[p]}</td>`;
                }
                html += `</tr>`;
            });
            html += `</tbody></table>`;
            additionalListsDiv.innerHTML += html;
    });
}

function onLogicBlockHoverHandler(ev) {
    if(ev.altKey) {
        beige.html(ev.target.dataset['stringified'])
            .css("left", ev.clientX + 5 + "px")
            .css("top", ev.clientY + window.scrollY + "px");
    }
    beige.toggleClass("beige-hidden", !ev.altKey);
}

function buildHtmlForDslElement(dsl) {
    if(dsl.type === 'PLAIN') {
        condsHtml += !rowIsOpen ? `<tr class="${boolClassMap.get(dsl.state)} ${dsl.state}">` : '';
        condsHtml += `<td class="${boolClassMap.get(dsl.state)} ${dsl.state} dsl-plain shadowed" data-stringified="${dsl.stringified}"><span class="operand-title">${dsl.title || ''}</span><br><span class="operand-condition">${dsl.cond || ''}</span></td>`;
        condsHtml += `</tr>`;
        rowIsOpen = false;
    } else {
        let rowSpan = dsl.childrenCount * 2 - 1;

        if(!rowIsOpen) {
            condsHtml += `<tr class="${boolClassMap.get(dsl.state)} ${dsl.state}">`;
            rowIsOpen = true;
        }

        condsHtml += `<td rowspan="${rowSpan}" class="${boolClassMap.get(dsl.state)} ${dsl.state} dsl-block-td shadowed" data-stringified="${dsl.stringified}"><span class="dsl-block"><span class="operation-name">${dsl.type}</span><br><span class="operation-title">${dsl.title || ''}</span></span></td>`;

        for(let i = 0; i < dsl.subConds.length; i++) {
            buildHtmlForDslElement(dsl.subConds[i]);

            if(i < (dsl.subConds.length - 1)) {
                condsHtml += `<tr class="separator"><td>&nbsp;</td></tr>`;
            }
        }
    }
}
