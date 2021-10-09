var tree = [];
var source = '';
function repeatText(num, txt)
{
    var ret = '';
    for(var i = 1; i<num; i++)
    {
        ret += txt;
    }
    return ret;
}
$(document).ready(function(e){
    source = $('.file-manager').attr('data-source');
    $.ajax({
        'type': 'GET',
        'url': source,
        'dataType':'json',
        success: function (data) {
            tree = data;
            renderFile(tree, '');
            $('.parentdir').append(renderDirrectory(tree, ''));
            $('.select-path').empty();
            $('.select-path').append('<option value=""></option>');
            $('.parentdir').find('li').each(function(e3){
                var path = $(this).attr('data-path');
                var opt = $('<option />');
                opt.append(path);
                opt.attr('value', path);
                $('.select-path').append(opt);
            })       
        }
    });
    $(document).on('click', '.dir a', function(e2){
        var li = $(this).closest('li');
        if(li.hasClass('expanded'))
        {
            li.removeClass('expanded');
        }
        else
        {
            li.addClass('expanded');
        }
        var path = li.attr('data-path');
        $('.select-path').val(path);
        renderFile(listFile(path), path);
    })
    $(document).on('click', '.file-container table tbody .dir-item a', function(e2){
        var tr = $(this).closest('tr');
        var path = tr.attr('data-path');
        $('.select-path').val(path);
        if(path == '')
        {
            renderFile(tree, '');
        }
        else
        {
            expandDir(path);
            renderFile(listFile(path), path);
        }
    });
    $(document).on('change', '.select-path', function(e2){
        var path = $(this).val();
        if(path == '')
        {
            renderFile(tree, '');
        }
        else
        {         
            expandDir(path);
            openDir(path);
            renderFile(listFile(path), path);
        }
    });
});
function expandDir(path)
{
    $('.dir').find('li[data-path="'+path+'"]').addClass('expanded');
}
function openDir(path)
{
    var paths = path.split('/');
    while(paths.length > 0)
    {
        expandDir(paths.join('/'));
        paths.pop();
    }
}
function getParentDir(path)
{
    var paths = path.split('/');
    paths.pop();
    return paths.join('/');
}
function renderFile(data, parentDir)
{
    if(typeof data != 'undefined')
    {
        $('.file-container table tbody').empty();
        var no = 1;
        no = renderRowParentDir(data, parentDir, no);
        no = renderRowDir(data, parentDir, no);
        no = renderRowFile(data, parentDir, no);      
    }
}
function renderRowParentDir(data, parentDir, no)
{
    var tr;
    if(parentDir != '')
    {
        tr = $('<tr><td><input type="checkbox" disabled></td>'+
        '<td><span class="icon icon-dir"></span></td>'+
        '<td><a href="javascript:;"><span class="updir">&larrhk;</span></a></td>'+
        '<td></td>'+
        '<td class="file-modify"></td>'+
        '<td>[DIR]</td></tr>');
        tr.addClass('dir-item');
        tr.attr('data-path', getParentDir(parentDir));
        $('.file-container table tbody').append(tr);
        no++;
    }
    return no;
}
function renderRowDir(data, parentDir, no)
{
    var dt;
    var i;
    var path;
    var modified;
    var tr;
    for(i in data)
    {
        if(parentDir != '')
        {
            path = parentDir+'/'+data[i].name;
        }
        else
        {
            path = data[i].name;
        }
        if(data[i].type == 'dir')
        {
            dt = new Date();
            dt.setTime(data[i].modified);
            modified = dt.toISOString();
            modified = modified.substring(0, 19);
            modified = modified.replace('T', ' ');
            tr = $('<tr><td><input type="checkbox" class="check-all" name="id[]" value="'+path+'"></td>'+
            '<td><span class="icon icon-dir"></span></td>'+
            '<td><a href="javascript:;">'+data[i].name+'</a></td>'+
            '<td></td>'+
            '<td class="file-modify">'+modified+'</td>'+
            '<td>[DIR]</td></tr>');
            tr.addClass('dir-item');
            tr.attr('data-path', path);
            $('.file-container table tbody').append(tr);
            no++;
        }
    }
    return no;
}
function renderRowFile(data, parentDir, no)
{
    var dt;
    var i;
    var path;
    var modified;
    var tr;
    for(i in data)
    {
        if(parentDir != '')
        {
            path = parentDir+'/'+data[i].name;
        }
        else
        {
            path = data[i].name;
        }
        if(data[i].type == 'file')
        {
            dt = new Date();
            dt.setTime(data[i].modified);
            modified = dt.toISOString();
            modified = modified.substring(0, 19);
            modified = modified.replace('T', ' ');
            var size = Math.ceil(data[i].size/1024);
            var pathDownload = getParentDir(source)+'/download/'+path;
            tr = $('<tr><td><input type="checkbox" class="check-all" name="id[]" value="'+path+'"></td>'+
            '<td><span class="icon icon-file"></span></td>'+
            '<td><a href="'+pathDownload+'">'+data[i].name+'</a></td>'+
            '<td align="right">'+size+'</td>'+
            '<td class="file-modify">'+modified+'</td>'+
            '<td>[FILE]</td></tr>');
            tr.addClass('file-item');
            tr.attr('data-path', path);
            $('.file-container table tbody').append(tr);
            no++;
        }
    }
    return no;
}
function listFile(path)
{
    var paths = path.split('/');
    var result = [];
    var obj = JSON.parse(JSON.stringify(tree));
    for(var i in paths)
    {
        if(paths[i] == '')
        {
            continue;
        }
        obj = getChildByName(obj, paths[i]);
        if(obj != null)
        {
            result = obj;
        }
        console.log(obj);
        
    }
    return result;

}
function getChildByName(data, name)
{
    if(typeof data != 'undefined')
    {
        for(var i in data)
        {
            if(data[i].name == name)
            {
                return data[i].child;
            }
        }
    }
    else
    {
        return null;
    }
}
function hashSubdir(data)
{
    var childs = data.child;
    for(var i in childs)
    {
        if(childs[i].type == 'dir')
        {
            return true;
        }
    }
    return false;
}
function renderDirrectory(data, parentDir, level)
{
    level = level || 1;
    var list = ''; 
    var path = '';
    for(var i in data)
    {
        if(data[i].type == 'dir')
        {
            var li = '';
            if(parentDir != '')
            {
                path = parentDir+'/'+data[i].name;
            }
            else
            {
                path = data[i].name;
            }
            if(hashSubdir(data[i]))
            {
                li = '<li data-level="'+level+'" data-name="'+data[i].name+'" data-path="'+path+'" class="icon icon-dir"><a href="javascript:;">'+data[i].name+'</a><ul>'+renderDirrectory(data[i].child, path, level+1)+'</ul></li>';
            }
            else
            {
                li = '<li data-level="'+level+'" data-name="'+data[i].name+'" data-path="'+path+'" class="icon icon-dir"><a href="javascript:;">'+data[i].name+'</a></li>';
            }
            list += li;
        }
    }
    return list;
}