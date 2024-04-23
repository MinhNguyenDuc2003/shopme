function addNextDetailSection(){
	allDivDetails = $("[id^='divDetails']"); 
	divDetailCount = allDivDetails.length;
	htmlDetailSection=`
		<div class="form-inline" id="divDetails${divDetailCount}">
			<input type="hidden" name="detailIDs" value="0" />
			<label class="m-3">Name: </label>
			<input type="text" class="form-control w-25" name="detailNames" maxlength="255"/>
			<label class="m-3">Value: </label>
			<input type="text" class="form-control w-25" name="detailValues" maxlength="255"/>
		</div>
	`;
	$("#divProductDetails").append(htmlDetailSection);
	previousDivDetailSection = allDivDetails.last();
	previousDivDetailId = previousDivDetailSection.attr("id");	
	
	htmlLinkRemove = `
		<a class="btn fas fa-times-circle fa-2x icon-dark"
			href="javascript:removeDetailSectionbyId('${previousDivDetailId}')"			
			title="Remove this detail"></a>
	`;
	$("input[name='detailsNames']").last().focus();
	previousDivDetailSection.append(htmlLinkRemove);
}

function removeDetailSectionbyId(id){
	$("#"+id).remove(); 
}

$(document).ready(function() {
	$("a[name='linkRemoveDetail']").each(function(index) {
		$(this).click(function() {
			removeDetailSectionByIndex(index);
		});
	});

});

function removeDetailSectionByIndex(index) {
	$("#divDetail" + index).remove();
}