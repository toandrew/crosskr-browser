$(function(){
	resetBtn();
	$(window).resize(function(){
		resetBtn();
	})

	$(".tabList>li").live("click",function(){
		var _this = $(this);
		var index = _this.index();
		_this.tabClass("sel");
		_this.parents(".tabList").siblings(".tabCnts").children(".tabCnt").eq(index).tabClass("block");
	})

	$(".tabCntRange>a").live("click",function(){
		$(".tabCntRange>a").removeClass("sel");
		$(this).addClass("sel");
		$("#curNum").text($(this).text());
	})

	var rangeListW = 0;
	$("#tabListRange>li").each(function(i){
		rangeListW += $(this).width()+26;
		if(i == $("#tabListRange>li").length-1){
			$("#tabListRange").width(rangeListW);
		}
	})

	$("#source").click(function(){
		$(this).parents(".cnt").toggleClass("sourceOpen");
		return false;
	})
	$(document).click(function(){
		$("#source").parents(".cnt").removeClass("sourceOpen");
	})
	
	$("#search").click(function(){
		$(this).parents(".searchSelectWrap").toggleClass("searchOpen");
		return false;
	})
	$(document).click(function(){
		$("#search").parents(".searchSelectWrap").removeClass("searchOpen");
	})
	$(".searchList>span").live("click",function(){
		var _this = $(this);
		_this.tabClass("sel");
		$("#searchSource").val(_this.attr("data-value"));
		$("#search").text(_this.attr("data-value"));
	})
	/*$(".searchSelect").change(function(){
		$("#curLogo").css({"background-image":"url('"+$(this).find("option:selected").attr("data-logo")+"')"})
	})*/

	if($("#tabListRange").length > 0){
		var touchObj = document.getElementById("tabListRange");
		var start_left;
		var org_left;
		var max_left = 0;
		var min_left = $(window).width()-$(touchObj).width();

        touchObj.addEventListener('touchstart', function(event) {
            if (event.targetTouches.length == 1) {
                var touch = event.targetTouches[0];
                start_left = touch.pageX;
                org_left = parseInt($(touchObj).css("left"));
            }
        }, false);

        touchObj.addEventListener('touchmove', function(event) {
            event.preventDefault();
            if (event.targetTouches.length == 1) {
                var touch = event.targetTouches[0];
                var left = parseFloat(touch.pageX - start_left + org_left);
                if(left <= max_left && left >= min_left){
                	touchObj.style.left = String(left) + 'px';
                }
            }
        }, false);

        touchObj.addEventListener('touchend', function(event) {
            var touch = event.changedTouches[0];
            var moveDis = parseFloat(touch.pageX - start_left);
            if(moveDis <= -15){
                
            }
            else if(moveDis >= 15){
                
            }
            else{
                $(touchObj).css({"left":org_left+"px"});
            }
            event.stopPropagation();
        });
	}
})

function resetBtn(){
	var winW = $(window).width();
	var btnW = (winW-48)/5;
	var btnH = btnW/1.3;
	$(".tabCntRange>a").width(btnW).height(btnH).css({"line-height":(btnH-2)+"px"});
}