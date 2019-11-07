import ReactMapboxGl from 'react-mapbox-gl';
import { FitBounds } from './model';

export const Mapbox = ReactMapboxGl({
  minZoom: 8,
  maxZoom: 15,
  doubleClickZoom: false,
  touchZoomRotate: false,
  accessToken: 'pk.eyJ1IjoiamNhc2FscnVpeiIsImEiOiJjazJkNWk5cjgwc29kM2hwNDBhZDl2a3AxIn0.aDwI1MEYvA_swKkLYIMXbA'
});

export const flyToOptions = {
  speed: 0.8
};

export const maxBounds = [[-0.481747846041145, 51.3233379650232], [0.23441119994140536, 51.654967740310525]] as FitBounds;
