// Pan + zoom para diagramas Mermaid usando svg-pan-zoom
document.addEventListener("DOMContentLoaded", function () {
  function initZoom() {
    document.querySelectorAll(".mermaid svg").forEach(function (svg, i) {
      if (svg.dataset.panzoom) return;
      svg.dataset.panzoom = "1";
      svg.style.maxWidth = "100%";
      svg.style.cursor = "grab";

      var scale = 1, originX = 0, originY = 0;
      var dragging = false, startX, startY, lastX = 0, lastY = 0;

      function applyTransform() {
        svg.style.transform =
          "translate(" + originX + "px," + originY + "px) scale(" + scale + ")";
        svg.style.transformOrigin = "0 0";
      }

      svg.addEventListener("wheel", function (e) {
        e.preventDefault();
        var delta = e.deltaY > 0 ? 0.85 : 1.15;
        scale = Math.min(Math.max(scale * delta, 0.3), 8);
        applyTransform();
      }, { passive: false });

      svg.addEventListener("mousedown", function (e) {
        dragging = true;
        startX = e.clientX - lastX;
        startY = e.clientY - lastY;
        svg.style.cursor = "grabbing";
      });

      window.addEventListener("mousemove", function (e) {
        if (!dragging) return;
        lastX = e.clientX - startX;
        lastY = e.clientY - startY;
        originX = lastX;
        originY = lastY;
        applyTransform();
      });

      window.addEventListener("mouseup", function () {
        dragging = false;
        svg.style.cursor = "grab";
      });

      // Doble clic resetea
      svg.addEventListener("dblclick", function () {
        scale = 1; originX = 0; originY = 0; lastX = 0; lastY = 0;
        applyTransform();
      });
    });
  }

  // Mermaid puede tardar en renderizar — observar el DOM
  var observer = new MutationObserver(initZoom);
  observer.observe(document.body, { childList: true, subtree: true });
  initZoom();
});
