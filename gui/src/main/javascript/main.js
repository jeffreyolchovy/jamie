function selectButton(id) {
  $('label[for="'+ id + '"]').addClass('button-pressed');
}

function deselectButtons() {
  $('label.button').removeClass('button-pressed');
}

function toggleButtons(event) {
  let eventTarget = $(event.target);
  let eventTargetId = eventTarget.attr('id');
  let eventTargetValue = eventTarget.attr('value');
  deselectButtons();
  selectButton(eventTargetId);
}

function resetPanes() {
  $('#transcript-pane').empty();
  $('#entity-pane iframe').attr('src', 'about:blank');
}

function displayLoadingAnimation() {
  $('#transcript-pane').html(
    '<div class="spinner"><div class="spinner-container"><span class="spinner-animation"></span></div></div>'
  );
}

function transcribe(event) {
  let eventTarget = $(event.target);
  let eventTargetValue = eventTarget.attr('value');
  toggleButtons(event);

  $.getJSON(
    'assets/data/' + eventTargetValue + '-transcript.json',
    function(transcripts, status, xhr) {
      xhr.onloadstart = function() {
        resetPanes();
        displayLoadingAnimation();
      };
      xhr.onload = function() {
        resetPanes();
      };
      getEntities(transcripts, function(entities) {
        typeTranscript(transcripts, entities);
      });
    }
  );
}

function upload(event) {
  let eventTarget = $(event.target);
  toggleButtons(event);

  let formData = new FormData();
  formData.append('audio-upload', eventTarget[0].files[0]);

  $.ajax({
    url: 'api/transcript',
    type: 'POST',
    data: formData,
    processData: false,
    contentType: false,
    beforeSend: function(xhr, settings) {
      xhr.onloadstart = function() {
        resetPanes();
        displayLoadingAnimation();
      };
      xhr.onload = function() {
        resetPanes();
      };
    },
    success: function(transcripts) {
      getEntities(transcripts, function(entities) {
        typeTranscript(transcripts, entities);
      });
    }
  });
}

function typeTranscript(transcripts, entities) {
  let target = $('#transcript-pane');
  let node = target[0];

  function typeWords(words, callback) {
    function typeIntoTarget(i) {
      let item = words[i];
      target.append(item.word + ' ');
      node.normalize();
      setTimeout(
        function(j) {
          if (callback && (i == words.length - 1)) {
            callback();
          } else if (j < words.length) {
            typeIntoTarget(j);
            surfaceEntities(entities);
          }
        },
        item.utteranceMillis,
        i + 1
      );
    }
    if (words.length) {
      typeIntoTarget(0);
      surfaceEntities(entities);
    }
  }

  if (transcripts.length > 1) {
    typeWords(
      transcripts[0].words,
      function() { typeTranscript(transcripts.slice(1), entities); }
    );
  } else if (transcripts.length == 1) {
    typeWords(
      transcripts[0].words
    );
  }
}

function getEntities(transcripts, callback) {
  let text = transcripts.map(function(item) { return item.text; }).join(' ');
  $.getJSON('api/entities?text=%22' + text + '%22', function(entities) {
    var sortedEntities = entities.sort(function(a, b) {
      if (a.entity.length > b.entity.length) {
        return -1;
      } else if (a.entity.length < b.entity.length) {
        return 1;
      } else {
        return 0;
      }
    });
    callback(entities);
  });
}

function surfaceEntities(entities) {
  let searchTarget = $('#transcript-pane');

  function helper(node) {
    var skip = 0;
    if (node.nodeType == 3) {
      $.each(entities, function(index, item) {
        let pos = node.data.indexOf(item.entity);
        if (pos >= 0) {
          let anchorText = node.splitText(pos);
          anchorText.splitText(item.entity.length);
          $(anchorText).wrap('<a class="entity" href="' + item.url + '" target="entity-iframe"></a>');
          skip++;
        }
      });
    } else if (node.nodeType == 1 && node.childNodes) {
      for (var i = 0; i < node.childNodes.length; ++i) {
        let child = node.childNodes[i];
        if (!$(child).hasClass('entity')) {
          i += helper(child);
        }
      }
    }
    return skip;
  }

  searchTarget.each(function() { helper(this); });
}
