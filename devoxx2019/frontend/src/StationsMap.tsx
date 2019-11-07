import React from 'react';
import { Layer, Feature, Popup } from 'react-mapbox-gl';
import { cycleImage } from './cycleImage';
import { Station } from './api';
import { FitBounds } from 'react-mapbox-gl/lib/map';
import { Mapbox, flyToOptions, maxBounds } from './settings';
import { CommentMarker } from './CommentMarker';
import { MarkerProps } from './model';
import { StationMapForm } from './StationsMapForm';

import './stationsMap.scss';

const layoutLayer = { 'icon-image': 'londonCycle' };
const images: any = ['londonCycle', cycleImage];

export interface MapProps {
  onCommentSubmit?: (comment: MarkerProps) => any;
  onStyleLoad?: (map: any) => any;
  onFormSubmit?: any;
  stations?: Station[];
  comments?: Array<MarkerProps>;
}

export interface MapState {
  fitBounds?: FitBounds;
  center: [number, number];
  zoom: [number];
  station?: Station;
  index?: number;
}

export class StationsMap extends React.Component<MapProps, MapState> {
  public state: MapState = {
    fitBounds: undefined,
    center: [-0.109970527, 51.49916347],
    zoom: [12],
    station: undefined,
    index: 0
  };

  private onDrag = () => {
    if (this.state.station) {
      this.setState({ station: undefined });
    }
  };

  private onToggleHover(cursor: string, event: any) {
    event.map.getCanvas().style.cursor = cursor;
  }

  private markerClick = (station: Station, index: number) => {
    const isNew = station !== this.state.station;
    this.setState({
      station: isNew ? station : undefined,
      index: isNew ? index : 0
    });
  };

  private onStyleLoad = (map: any) => {
    const { onStyleLoad } = this.props;
    return onStyleLoad && onStyleLoad(map);
  };

  private onSubmit = (comment: Comment) => {
    this.props.onFormSubmit && this.props.onFormSubmit(comment);
    this.setState({
      station: undefined,
      index: 0
    });
  };

  public render() {
    const { fitBounds, center, zoom, station, index } = this.state;
    const { stations, comments } = this.props;

    return (
      <Mapbox
        style={'mapbox://styles/mapbox/light-v9'}
        onStyleLoad={this.onStyleLoad}
        fitBounds={fitBounds}
        maxBounds={maxBounds}
        center={center}
        zoom={zoom}
        onDrag={this.onDrag}
        containerStyle={{ flex: 1 }}
        flyToOptions={flyToOptions}>
        <>
          {comments &&
            comments.map((marker: any, index: number) => (
              <Popup coordinates={marker.coordinates} key={`${marker.id}-${index}`}>
                <CommentMarker {...marker.comment} />
              </Popup>
            ))}
        </>
        <Layer type="symbol" id="marker" layout={layoutLayer} images={images}>
          {stations &&
            stations.map((stationK, index) => (
              <Feature
                key={index}
                onMouseEnter={e => this.onToggleHover('pointer', e)}
                onMouseLeave={e => this.onToggleHover('', e)}
                onClick={e => this.markerClick(stations[index], index)}
                coordinates={stations[index].position}
              />
            ))}
        </Layer>
        <>
          {station && (
            <Popup coordinates={station.position} key={`${station.id}`}>
              <StationMapForm className={'flw-inMap-form'} station={index} onFormSubmit={(e: Comment) => this.onSubmit(e)} />
            </Popup>
          )}
        </>
      </Mapbox>
    );
  }
}
