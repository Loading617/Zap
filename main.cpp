#include "Zap.h"

Zap::Zap()
{
	CtrlLayout(*this, "Zap!");
	Sizeable().MinimizeBox().MaximizeBox();
}

GUI_APP_MAIN
{
	Zap().Run();
}
