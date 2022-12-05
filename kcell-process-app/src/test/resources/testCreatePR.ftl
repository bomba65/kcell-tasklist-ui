<#assign resolutions = '[{"processInstanceId":"82ce5ef3-8329-11e7-8181-da6044b09f14","assignee":"demo","resolution":"approved","taskId":"83a118ac-8329-11e7-8181-da6044b09f14","$$hashKey":"object:692"}]'>
<#assign resolutionsObj = resolutions?eval>
${resolutions}
<table>
    <thead>
    <th>Activity</th>
    <th>Assignee</th>
    <th>Resolution</th>
    <th>Comment</th>
    </thead>
    <tbody>
    <#list resolutionsObj as resolution>
    <tr>
        <td>${(history["tasksMap"][resolution.taskId].name)!"N/A"}</td>
        <td>${resolution.assignee!"N/A"}</td>
        <td>${resolution.resolution!"N/A"}</td>
        <td></td>
        <td>${resolution.comment!"N/A"}</td>
    </tr>
    </#list>
    </tbody>
</table>