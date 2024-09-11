//
// Visualization for Zap!.
//

#include "Whoosh.h"
#include "androidtv.h"
#include "../Zap!/visualization.h"

Whoosh::Whoosh(Context* context) : m_context(context) {
    m_mediaPlayer = new MediaPlayer();
    m_visualization = new Visualization(context);
}

Whoosh::~Whoosh() {
    delete m_mediaPlayer;
    delete m_visualization;
}

void Whoosh::play(const std::string& mediaFilePath) {
    m_mediaPlayer->setDataSource(mediaFilePath);
    m_mediaPlayer->prepare();
    m_mediaPlayer->start();
    m_visualization->start(m_mediaPlayer);

    // Implement event handling for media player and visualization

    // Example:
    m_mediaPlayer->setOnCompletionListener([this]() {
        m_visualization->stop();
    });

}