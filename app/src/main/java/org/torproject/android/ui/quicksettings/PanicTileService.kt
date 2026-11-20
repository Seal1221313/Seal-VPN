package org.torproject.android.ui.quicksettings

import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import org.torproject.android.ui.Panic

class PanicTileService : TileService() {
    override fun onStartListening() {
        qsTile?.apply {
            state = Tile.STATE_INACTIVE
            updateTile()
        }
    }

    override fun onClick() {
        Panic.trigger(applicationContext)
    }
}
