#ifndef _Zap_Zap_h
#define _Zap_Zap_h

#include <CtrlLib/CtrlLib.h>

using namespace Upp;

#define LAYOUTFILE <Zap/Zap.lay>
#include <CtrlCore/lay.h>

class Zap : public WithZapLayout<TopWindow> {
public:
	Zap();
};

#endif
