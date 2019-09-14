function FlowerBackground(count) {
    if (count != undefined) {
        this.count = count;
    }
}

FlowerBackground.prototype = {
    render: undefined,
    width: undefined,
    height: undefined,
    canvas: undefined,
    flowerShown: true,
    initThree: function () {
        this.createCanvas();
        renderer = new THREE.WebGLRenderer({
            antialias: true,
            alpha: true
        });
        renderer.setClearColor(0xffffff, 0);
        renderer.setSize(width, height);
        canvas.appendChild(renderer.domElement);
        renderer.shadowMap.enabled = true;
    },
    createCanvas: function () {
        canvas = document.createElement("div");
        canvas.setAttribute(
            "style",
            "position:absolute;" +
            "z-index:1;" +
            "left:0;" +
            "top:0;" +
            "display: block;" +
            "pointer-events:none"
        );
        document.body.appendChild(canvas);
        width = canvas.width = window.innerWidth - 5;
        height = canvas.height = window.innerHeight - 5;
    },
    camera: undefined,
    initCamera: function () {
        camera = new THREE.PerspectiveCamera(45, width / height, 1, 10000);
        camera.position.x = 0;
        camera.position.y = 0;
        camera.position.z = 1000;
        camera.up.x = 0;
        camera.up.y = 0;
        camera.up.z = 0;
        camera.lookAt(0, 0, 0);
    },
    scene: undefined,
    initScene: function () {
        scene = new THREE.Scene();
    },
    initLight: function () {
        let light;

        light = new THREE.DirectionalLight(0xffffff, 1.2);
        light.position.set(-1, 1, 1);
        scene.add(light);

        light = new THREE.AmbientLight(0xffffff, 1);
        scene.add(light);
    },
    startAnimation: function () {
        this.initThree();
        this.initCamera();
        this.initScene();
        this.initLight();
        this.initObject();
        window.addEventListener("resize", this.onWindowResize);

        _this = this;
        this.animation();
    },
    onWindowResize: function () {
        width = canvas.width = window.innerWidth - 5;
        height = canvas.height = window.innerHeight - 5;

        camera.aspect = width / height;
        camera.updateProjectionMatrix();
        renderer.setSize(width, height);
    },
    animation: function () {
        for (var i = 0; _this.flowerShown && i < meshes.length; i++) {
            var mesh = meshes[i];
            if (mesh.position.x < -width || mesh.position.y < -height) {
                _this.resetMesh(mesh);
            }
            mesh.rotation.x += mesh.rotationXVelocity;
            mesh.rotation.y += mesh.rotationYVelocity;
            mesh.rotation.z += mesh.rotationZVelocity;
            mesh.position.x += mesh.moveXVelocity;
            mesh.position.y += mesh.moveYVelocity;
            mesh.position.z += mesh.moveZVelocity;
        }

        if (_this.flowerShown) {
            camera.lookAt(0, 0, 0);
            renderer.render(scene, camera);
            requestAnimationFrame(_this.animation);
        }
    },
    meshes: undefined,
    count: 20,
    initObject: function () {
        meshes = new Array();
        var total = 0;
        var url = $('#three-flower').data('urls');
        new THREE.GLTFLoader().load(url, function (obj) {
            var baseGeometry = obj.scene.children[0].geometry;
            var interval = setInterval(function () {
                var geometry = baseGeometry.clone();
                var material = new THREE.MeshLambertMaterial({
                    color: 0xff0000 + Math.random() * 0x00ffff
                });
                var mesh = new THREE.Mesh(geometry, material);

                // mesh.castShadow = true;
                // mesh.receiveShadow = true;

                mesh.scale.set(12, 12, 12);
                _this.resetMesh(mesh);
                scene.add(mesh);
                meshes.push(mesh);

                if (++total >= _this.count) {
                    clearInterval(interval);
                }
            }, 3000)
        });
    },
    resetMesh: function (mesh) {
        if (Math.random() > 0.5) {
            mesh.position.x = width * (Math.random() - 0.5);
            mesh.position.y = height / 2 + 0.1 * height
        } else {
            mesh.position.x = width * Math.random() + 0.1 * width;
            mesh.pisition = height / 2
        }
        mesh.position.z = 50;
        mesh.moveXVelocity = -0.5 - 0.5 * Math.random();
        mesh.moveYVelocity = -0.5 - 0.5 * Math.random();
        mesh.moveZVelocity =
            -0.5 - (Math.random() < 0.5 ? 0.5 : -0.5) * Math.random();

        mesh.rotationXVelocity = 0.005;
        mesh.rotationYVelocity = 0.005;
        mesh.rotationZVelocity = 0.005;
    },
    triggerShown() {
        _this.flowerShown = !_this.flowerShown;
        if (_this.flowerShown) {
            camera.lookAt(0, 0, 0);
            renderer.render(scene, camera);
            requestAnimationFrame(_this.animation);
        } else {
            camera.lookAt(0, 0, 2000);
            renderer.render(scene, camera);
        }
        return _this.flowerShown
    }
};