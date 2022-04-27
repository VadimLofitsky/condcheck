document.addEventListener("DOMContentLoaded", start);

var sources;
var condsTable;
var beige;

let boolClassMap = new Map([[false, 'bool-false'], [true, 'bool-true']]);
var html = '';
var rowIsOpen = false;

function start() {
    if(!localStorage.getItem('lastPatientId')) localStorage['lastPatientId'] = 2012;

    sources = $("#sources");
    sources[0].style.setProperty("--sew", document.scrollingElement.clientWidth / 100 + "px");
    sources.css("width", "calc(100% - var(--sew))");

    condsTable = $("#condsTable>tbody")[0];
    beige = $("#beige");

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
    $.ajax({
        url: "http://localhost:9998/rest",
        method: "GET",
        async: true,
        cache: false,
        data: serializeForm(),
        dataType: "json",
        success: (response) => {
            console.log("***************************************************************************");
            console.log(response);
            console.log("***************************************************************************");
            drawConds(response);
        }
    });
}

function serializeForm() {
    return {
        patientId: $("input#patientId")[0].value,
        isHfRiskFactor: ($("input#isHfRiskFactor")[0].checked || false)
    };
}

function drawConds(data) {
    updateSourceDataView(data.sources);
    $("#stringified")[0].innerText = data.dsl.stringified;

    html = '';
    rowIsOpen = false;
    buildHtmlForDslElement(data.dsl);
    condsTable.innerHTML = html;

    $("td.dsl-plain").add($("td.dsl-block-td"))
        .mousemove(onLogicBlockHoverHandler)
        .mouseout(() => beige.toggleClass("beige-hidden", true));
}

function updateSourceDataView(data) {
    let sourcesDivHtml = '<ul>';
    for(var p in data) {
        if(p === 'fieldValues') continue;
        sourcesDivHtml += `<li><span class="sources-param-name">${p}</span> = <span class="sources-param-value">${data[p]}</span></li>`
    }
    sourcesDivHtml += '</ul';
    sources.html(sourcesDivHtml);
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
        html += !rowIsOpen ? `<tr class="${boolClassMap.get(dsl.state)} ${dsl.state}">` : '';
        html += `<td class="${boolClassMap.get(dsl.state)} ${dsl.state} dsl-plain shadowed" data-stringified="${dsl.stringified}"><span class="operand-title">${dsl.title || ''}</span><br><span class="operand-condition">${dsl.cond || ''}</span></td>`;
        html += `</tr>`;
        rowIsOpen = false;
    } else {
        let rowSpan = dsl.childrenCount * 2 - 1;

        if(!rowIsOpen) {
            html += `<tr class="${boolClassMap.get(dsl.state)} ${dsl.state}">`;
            rowIsOpen = true;
        }

        html += `<td rowspan="${rowSpan}" class="${boolClassMap.get(dsl.state)} ${dsl.state} dsl-block-td shadowed" data-stringified="${dsl.stringified}"><span class="dsl-block"><span class="operation-name">${dsl.type}</span><br><span class="operation-title">${dsl.title || ''}</span></span></td>`;

        for(let i = 0; i < dsl.subConds.length; i++) {
            buildHtmlForDslElement(dsl.subConds[i]);

            if(i < (dsl.subConds.length - 1)) {
                html += `<tr class="separator"><td>&nbsp;</td></tr>`;
            }
        }
    }
}
