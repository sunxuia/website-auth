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
            var width = window.innerWidth, height = window.innerHeight;
            var offset = 30;
            var random = Math.random();
            if (random < 0.15) {
                // 左上角上沿 -> 左上角左沿
                startPositionX = (0.1 + Math.random() * 0.4) * width;
                startPositionY = 0;
                endPositionX = -offset;
                endPositionY = (0.1 + Math.random() * 0.4) * height;
            } else if (random < 0.5) {
                // 右上角上沿-> 左下角左沿
                startPositionX = (Math.random() * 0.5 + 0.5) * width;
                startPositionY = 0;
                endPositionX = -offset;
                endPositionY = (Math.random() * 0.5 + 0.5) * height;
            } else if (random < 0.85) {
                // 右上角右沿 -> 左下角下沿
                startPositionX = width;
                startPositionY = Math.random() * 0.5 * height;
                endPositionX = Math.random() * 0.5 * width;
                endPositionY = height + offset;
            } else {
                // 右下角右沿 -> 右下角下沿
                startPositionX = width;
                startPositionY = (Math.random() * 0.4 + 0.5) * height;
                endPositionX = (Math.random() * 0.4 + 0.5) * width;
                endPositionY = height + offset;
            }
            var duration = 10 * Math.sqrt(
                Math.pow(startPositionX - endPositionX, 2)
                + Math.pow(startPositionY - endPositionY, 2))
                + Math.random() * 5000;
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