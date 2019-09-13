$(function () {
    function snowflake() {
        var flakeContainer = $('#snowflake');
        var flowerUrls = flakeContainer.data('urls').split(',');
        var flowerObjs = [];
        for (var i = 0; i < flowerUrls.length; i++) {
            flowerObjs.push($('<div class="snowbox" />').css({
                'backgroundImage': 'url(' + flowerUrls[i] + ')'
            }));
        }

        function createSnowBox() {
            var obj = flowerObjs[[Math.floor(Math.random() * flowerObjs.length)]];
            // console.log('create ', url);
            return obj.clone().addClass('snowRoll');
        }

        // 开始飘花
        setInterval(function () {
            if (!cssStart) {
                return;
            }
            // 运动的轨迹
            var startPositionX, startPositionY, endPositionX, endPositionY;
            if (Math.random() > 0.5) {
                // 右上角上沿-> 左下角左沿
                startPositionX = (Math.random() / 2 + 0.5) * window.innerWidth;
                startPositionY = 0;
                endPositionX = -30;
                endPositionY = (Math.random() / 2 + 0.5) * window.innerHeight;
            } else {
                // 右上角右沿 -> 左下角下沿
                startPositionX = window.innerWidth;
                startPositionY = Math.random() * window.innerHeight / 2;
                endPositionX = (Math.random() / 2) * window.innerWidth;
                endPositionY = window.innerHeight + 30;
            }
            var duration = 10 * Math.sqrt(
                window.innerHeight * window.innerHeight + window.innerWidth * window.innerHeight) + Math.random()
                * 5000;
            var randomOpacity = Math.random() * 0.5 + 0.5;
            var flake = createSnowBox();
            flake.css({
                left: startPositionX,
                top: startPositionY,
                opacity: randomOpacity
            });
            flakeContainer.append(flake);
            flake.transition({
                left: endPositionX,
                top: endPositionY,
                opacity: 0.7
            }, duration, 'ease-out', function () {
                $(this).remove() // 结束后删除
            });
        }, 500);
    }

    snowflake()
});

var cssStart = false;

function triggerRun() {
    cssStart = !cssStart;
    var snowflake = $('#snowflake');
    for (let child of snowflake.children()) {
        if (cssStart) {
            child.style.webkitAnimationPlayState = 'running';
            child.style.visibility = 'visible'
        } else {
            child.style.webkitAnimationPlayState = 'paused';
            child.style.visibility = 'hidden'
        }
    }
    return cssStart
}