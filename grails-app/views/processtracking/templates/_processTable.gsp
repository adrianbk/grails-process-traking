<table class="process-table">
    <tr>
        <g:each in="${displayProperties}" var="entry">
            <th>${entry.value}</th>
        </g:each>
    </tr>
    <g:each in="${processList}" var="process" status="i">
        <tr data-process-id="${process.id}" class="${i % 2 == 0 ? 'even' : 'odd'}">
            <g:each in="${displayProperties}" var="entry">
                <td>${process."${entry.key}"}</td>
            </g:each>
        </tr>
    </g:each>
</table>