$(document).ready(function () {
  const maxLines = 50;
  const leftSide = $('#left-side');
  const rightSide = $('#right-side');

  for (let i = 1; i < maxLines; i++) {
    if (i <= 17) {
      leftSide.append(
        `<span style="background-color: rgb(245, ${
          44 + i * 12
        }, 228)" class="eq-item"></span>`
      );
      rightSide.append(
        `<span style="background-color: rgb(245, ${
          44 + i * 12
        }, 228)" class="eq-item"></span>`
      );
    } else if (i > 17 && i <= 34) {
      leftSide.append(
        `<span style="background-color: rgb(102, 255, ${
          153 - i * 2
        })" class="eq-item"></span>`
      );
      rightSide.append(
        `<span style="background-color: rgb(102, 255, ${
          153 - i * 2
        })" class="eq-item"></span>`
      );
    } else if (i > 34 && i <= 50) {
      leftSide.append(
        `<span style="background-color: rgb(45, 253, ${
          255 - i
        })" class="eq-item"></span>`
      );
      rightSide.append(
        `<span style="background-color: rgb(45, 253, ${
          255 - i
        })" class="eq-item"></span>`
      );
    }
  }
});
