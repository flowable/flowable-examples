import React from 'react';
import classNames from 'classnames';

import './rating.scss';

var stars = new Array(5).fill(undefined);

type RatingProps = {
  rate: number;
  onClick?: (rate: number) => void;
};

export const Rating = (props: RatingProps) => (
  <div className="flw-rating">{stars.map((el, i) => renderStar(i, props.rate, props.onClick))}</div>
);

const renderStar = (i: number, rate: number, onClick?: (rate: number) => void) => {
  const starClass = classNames('flw-star', {
    selected: i < rate,
    enabled: onClick
  });

  return <div key={`start-${i}`} className={starClass} onClick={() => onClick && onClick(i + 1)} />;
};
