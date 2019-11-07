import React from 'react';

import './switch.scss';
type SwitchProps = {
  isOn: boolean;
  handleToggle: any;
};

export const Switch = (props: SwitchProps) => {
  return (
    <>
      <input
        checked={props.isOn}
        onChange={props.handleToggle}
        className="flw-switch-checkbox"
        id={`flw-switch-new`}
        type="checkbox"
      />
      <label className="flw-switch-label" htmlFor={`flw-switch-new`}>
        <span className={`flw-switch-button`} />
      </label>
    </>
  );
};
